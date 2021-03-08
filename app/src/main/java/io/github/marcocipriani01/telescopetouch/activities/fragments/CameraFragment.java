/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.tabs.TabLayout;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDISwitchElement;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.marcocipriani01.livephotoview.PhotoView;
import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.ProUtils;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.PIPCameraViewerActivity;
import io.github.marcocipriani01.telescopetouch.activities.util.ImprovedSpinnerListener;
import io.github.marcocipriani01.telescopetouch.activities.util.SimpleAdapter;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;
import io.github.marcocipriani01.telescopetouch.indi.INDICamera;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class CameraFragment extends ActionFragment implements INDICamera.CameraListener,
        CompoundButton.OnCheckedChangeListener, Toolbar.OnMenuItemClickListener, ConnectionManager.ManagerListener {

    private static final String TAG = TelescopeTouchApp.getTag(CameraFragment.class);
    private static INDIDevice selectedCameraDev = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<INDICamera> cameras = new ArrayList<>();
    private SharedPreferences preferences;
    private SwitchCompat fitsStretchSwitch;
    private TextView fileSizeText;
    private TextView dimensionsText;
    private TextView formatText;
    private TextView bppText;
    private TextView errorText;
    private PhotoView photoViewer;
    private ProgressBar progressBar;
    private Button exposeBtn;
    private Button abortBtn;
    private Spinner isoSpinner;
    private SwitchPropertyAdapter presetsAdapter;
    private SwitchPropertyAdapter isoAdapter;
    private SwitchPropertyAdapter frameTypeAdapter;
    private Spinner binningSpinner;
    private BinningValuesAdapter binningAdapter;
    private Spinner frameTypeSpinner;
    private Spinner saveModeSpinner;
    private AutoCompleteTextView exposureTimeField;
    private EditText prefixField;
    private EditText remoteFolderField;
    private final ImprovedSpinnerListener cameraSelectListener = new ImprovedSpinnerListener() {
        @Override
        protected void onImprovedItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            INDICamera camera = getCamera();
            if (camera != null) camera.removeListener(CameraFragment.this);
            selectedCameraDev = cameras.get(pos).device;
            camera = getCamera();
            if (camera != null) camera.addListener(CameraFragment.this);
            onCameraFunctionsChange();
        }
    };
    private Spinner cameraSelectSpinner;
    private CamerasArrayAdapter cameraSelectAdapter;
    private boolean pipSupported = false;
    private MenuItem pipMenuItem = null;
    private LayoutInflater inflater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_blob_viewer, container, false);
        pipSupported = PIPCameraViewerActivity.isSupported(context);
        if (pipSupported) setHasOptionsMenu(true);
        fitsStretchSwitch = rootView.findViewById(R.id.ccd_image_stretch_switch);
        fileSizeText = rootView.findViewById(R.id.blob_size);
        dimensionsText = rootView.findViewById(R.id.blob_dimensions);
        formatText = rootView.findViewById(R.id.blob_format);
        bppText = rootView.findViewById(R.id.blob_bpp);
        errorText = rootView.findViewById(R.id.blob_error_label);
        photoViewer = rootView.findViewById(R.id.blob_viewer);
        photoViewer.setMaximumScale(20f);
        progressBar = rootView.findViewById(R.id.blob_loading);
        exposeBtn = rootView.findViewById(R.id.ccd_expose_button);
        exposeBtn.setOnClickListener(this::capture);
        abortBtn = rootView.findViewById(R.id.ccd_abort_btn);
        abortBtn.setOnClickListener(this::abortCapture);
        isoSpinner = rootView.findViewById(R.id.ccd_iso_spinner);
        binningSpinner = rootView.findViewById(R.id.ccd_binning_spinner);
        frameTypeSpinner = rootView.findViewById(R.id.ccd_frame_type_spinner);
        saveModeSpinner = rootView.findViewById(R.id.ccd_image_receive_mode);
        saveModeSpinner.setAdapter(new SaveModeAdapter());
        prefixField = rootView.findViewById(R.id.ccd_file_prefix_field);
        remoteFolderField = rootView.findViewById(R.id.ccd_remote_folder_field);
        exposureTimeField = rootView.findViewById(R.id.ccd_exposure_time_field);
        View captureTab = rootView.findViewById(R.id.ccd_viewer_capture_tab),
                viewTab = rootView.findViewById(R.id.ccd_viewer_image_tab);
        rootView.<TabLayout>findViewById(R.id.ccd_viewer_tabs)
                .addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            captureTab.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    captureTab.setVisibility(View.VISIBLE);
                                }
                            });
                            viewTab.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    viewTab.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            captureTab.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    captureTab.setVisibility(View.GONE);
                                }
                            });
                            viewTab.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    viewTab.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
        cameraSelectAdapter = new CamerasArrayAdapter();
        cameraSelectSpinner = rootView.findViewById(R.id.ccd_selection_spinner);
        cameraSelectSpinner.setAdapter(cameraSelectAdapter);
        cameraSelectListener.attach(cameraSelectSpinner);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean stretch = preferences.getBoolean(ApplicationConstants.STRETCH_FITS_PREF, false);
        fitsStretchSwitch.setOnCheckedChangeListener(null);
        fitsStretchSwitch.setChecked(stretch);
        fitsStretchSwitch.setSelected(stretch);
        fitsStretchSwitch.setOnCheckedChangeListener(this);

        connectionManager.addManagerListener(this);
        cameras.clear();
        if (connectionManager.isConnected()) {
            cameras.addAll(connectionManager.indiCameras.values());
            cameraSelectAdapter.notifyDataSetChanged();
            if (cameras.isEmpty()) {
                selectedCameraDev = null;
                cameraSelectSpinner.setEnabled(false);
            } else {
                INDICamera selectedCamera = cameras.get(0);
                if (selectedCameraDev == null) selectedCameraDev = selectedCamera.device;
                selectedCamera.addListener(this);
                selectedCamera.setStretch(stretch);
                cameraSelectSpinner.setSelection(cameras.indexOf(selectedCamera));
                cameraSelectSpinner.setEnabled(true);
                Bitmap lastBitmap = selectedCamera.getLastBitmap();
                photoViewer.setImageBitmap(lastBitmap);
                if (lastBitmap == null) {
                    errorText.setText(R.string.no_incoming_data);
                    photoViewer.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    photoViewer.setVisibility(View.VISIBLE);
                    errorText.setVisibility(View.GONE);
                }
            }
        } else {
            selectedCameraDev = null;
            cameraSelectAdapter.notifyDataSetChanged();
            cameraSelectSpinner.setEnabled(false);
            errorText.setText(R.string.no_incoming_data);
            photoViewer.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        }
        onCameraFunctionsChange();
        progressBar.setVisibility(View.INVISIBLE);
        notifyActionChange();
    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.removeManagerListener(this);
        photoViewer.setImageBitmap(null);
        for (INDICamera camera : cameras) {
            camera.removeListener(this);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (pipSupported) {
            pipMenuItem = menu.add(R.string.floating_image);
            pipMenuItem.setIcon(R.drawable.picture_in_picture);
            pipMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private INDICamera getCamera() {
        if (selectedCameraDev == null) return null;
        return connectionManager.indiCameras.get(selectedCameraDev);
    }

    public void capture(View v) {
        //TODO hide keyboard
        INDICamera camera = getCamera();
        if (camera == null) {
            //TODO error
            onCameraFunctionsChange();
        } else {
            try {
                camera.setSaveMode((INDICamera.SaveMode) saveModeSpinner.getSelectedItem());
                if (camera.hasFrameTypes())
                    camera.setFrameType(((INDISwitchElement) frameTypeSpinner.getSelectedItem()));
                if (camera.hasBinning())
                    camera.setBinning(((Integer) binningSpinner.getSelectedItem()));
                camera.capture(exposureTimeField.getText().toString());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                //TODO error message
            }
        }
    }

    public void abortCapture(View v) {
        INDICamera camera = getCamera();
        if (camera == null) {
            //TODO error
            onCameraFunctionsChange();
        } else {
            try {
                camera.abort();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                //TODO error message
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item == pipMenuItem) {
            if (ProUtils.isPro) {
                if (PIPCameraViewerActivity.isVisible()) {
                    PIPCameraViewerActivity.finishInstance();
                } else if (((AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE))
                        .checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(),
                                context.getPackageName()) != AppOpsManager.MODE_ALLOWED) {
                    requestActionSnack(R.string.pip_permission_required);
                } else {
                    startActivity(new Intent(context, PIPCameraViewerActivity.class));
                }
            } else {
                requestActionSnack(R.string.pro_feature);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == fitsStretchSwitch) {
            if (ProUtils.isPro) {
                INDICamera camera = getCamera();
                if (camera != null) {
                    camera.setStretch(isChecked);
                    camera.reloadBitmap();
                }
                preferences.edit().putBoolean(ApplicationConstants.STRETCH_FITS_PREF, isChecked).apply();
            } else {
                fitsStretchSwitch.setOnCheckedChangeListener(null);
                fitsStretchSwitch.setSelected(false);
                fitsStretchSwitch.setChecked(false);
                fitsStretchSwitch.setOnCheckedChangeListener(this);
                ProUtils.toast(context);
            }
        }
    }

    @Override
    public boolean isActionEnabled() {
        INDICamera camera = getCamera();
        if (camera == null) return false;
        return camera.hasBitmap() && (!camera.isBitmapSaved());
    }

    @Override
    public int getActionDrawable() {
        return R.drawable.save;
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public void run() {
        new Thread(() -> {
            if (isActionEnabled()) {
                try {
                    Uri uri = Objects.requireNonNull(getCamera()).saveImage();
                    handler.post(() -> {
                        notifyActionChange();
                        requestActionSnack(R.string.saved_snackbar, R.string.view_image, v -> {
                            Intent intent = new Intent();
                            intent.setDataAndType(uri, "image/*");
                            intent.setAction(Intent.ACTION_VIEW);
                            startActivity(Intent.createChooser(intent, context.getString(R.string.open_photo)));
                        });
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Saving error", e);
                    handler.post(() -> requestActionSnack(R.string.saving_error));
                }
            } else {
                handler.post(() -> requestActionSnack(R.string.nothing_to_save));
            }
        }).start();
    }

    private void setBlobInfo(String[] info) {
        if (info == null) {
            fileSizeText.setText(R.string.unknown);
            dimensionsText.setText(R.string.unknown);
            formatText.setText(R.string.unknown);
            bppText.setText(R.string.unknown);
        } else if (info.length != 4) {
            throw new IllegalArgumentException();
        } else {
            String unknown = context.getString(R.string.unknown);
            fileSizeText.setText((info[0] == null) ? unknown : info[0]);
            dimensionsText.setText((info[1] == null) ? unknown : info[1]);
            formatText.setText((info[2] == null) ? unknown : info[2]);
            bppText.setText((info[3] == null) ? unknown : info[3]);
        }
    }

    private void onError(int errorMsg) {
        setBlobInfo(null);
        errorText.setText(errorMsg);
        photoViewer.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onRequestStoragePermission() {
        //TODO request permission
        return true;
    }

    @Override
    public void onCameraStateChange(Constants.PropertyStates state) {
        handler.post(() -> {
            if (exposeBtn == null) return;
            switch (state) {
                case OK:
                    exposeBtn.setEnabled(true);
                    exposeBtn.setTextColor(context.getResources().getColor(R.color.light_green));
                    break;
                case ALERT:
                    exposeBtn.setEnabled(true);
                    exposeBtn.setTextColor(context.getResources().getColor(R.color.light_red));
                    break;
                case BUSY:
                    exposeBtn.setEnabled(false);
                    exposeBtn.setTextColor(context.getResources().getColor(R.color.light_yellow));
                    break;
                case IDLE:
                    exposeBtn.setEnabled(true);
                    exposeBtn.setTextColor(Color.WHITE);
                    break;
            }
        });
    }

    @Override
    public void onCameraFunctionsChange() {
        boolean canCapture, hasPresets, canAbort, hasISO, hasBinning, hasUploadSettings, hasFrameTypes;
        INDICamera camera = getCamera();
        if (camera == null) {
            canCapture = hasPresets = canAbort = hasISO =
                    hasBinning = hasUploadSettings = hasFrameTypes = false;
        } else {
            canCapture = camera.canCapture();
            hasPresets = camera.hasPresets();
            canAbort = camera.canAbort();
            hasISO = camera.hasISO();
            hasBinning = camera.hasBinning();
            hasUploadSettings = camera.hasUploadSettings();
            hasFrameTypes = camera.hasFrameTypes();
        }
        presetsAdapter = hasPresets ? new SwitchPropertyStringAdapter(camera.availableExposurePresetsE) : null;
        if (exposureTimeField != null) {
            exposureTimeField.setEnabled(canCapture || hasPresets);
            exposureTimeField.setAdapter(presetsAdapter);
        }
        if (exposeBtn != null) exposeBtn.setEnabled(canCapture);
        if (abortBtn != null) abortBtn.setEnabled(canAbort);
        isoAdapter = hasISO ? new SwitchPropertyAdapter(camera.isoE) : null;
        if (isoSpinner != null) {
            isoSpinner.setEnabled(hasISO);
            isoSpinner.setAdapter(isoAdapter);
        }
        frameTypeAdapter = hasFrameTypes ? new SwitchPropertyAdapter(camera.frameTypesE) : null;
        if (frameTypeSpinner != null) {
            frameTypeSpinner.setEnabled(hasFrameTypes);
            frameTypeSpinner.setAdapter(frameTypeAdapter);
        }
        binningAdapter = hasBinning ? new BinningValuesAdapter() : null;
        if (binningSpinner != null) {
            binningSpinner.setEnabled(hasBinning);
            binningSpinner.setAdapter(binningAdapter);
        }
        //TODO: camera gain
        if (prefixField != null) prefixField.setEnabled(hasUploadSettings);
        if (remoteFolderField != null) remoteFolderField.setEnabled(hasUploadSettings);
    }

    @Override
    public void onImageLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoaded(Bitmap bitmap, String[] metadata) {
        setBlobInfo(metadata);
        if (bitmap == null) {
            errorText.setText(R.string.unsupported_format);
            photoViewer.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        } else {
            photoViewer.setImageBitmap(bitmap);
            photoViewer.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.INVISIBLE);
        notifyActionChange();
    }

    @Override
    public void onBitmapDestroy() {
        photoViewer.setImageBitmap(null);
        notifyActionChange();
    }

    @Override
    public void onINDICameraError(Throwable e) {
        Log.e("BLOBViewer", e.getLocalizedMessage(), e);
        if (e instanceof Error) {
            onError(R.string.out_of_memory);
        } else if (e instanceof FileNotFoundException) {
            onError(R.string.no_incoming_data);
        } else if (e instanceof EOFException) {
            onError(R.string.truncated_file);
        } else if (e instanceof IndexOutOfBoundsException) {
            onError(R.string.unsupported_color_fits);
        } else if (e instanceof UnsupportedOperationException) {
            onError(R.string.unsupported_bit_depth);
        } else if (e instanceof IllegalStateException) {
            onError(R.string.invalid_fits_image);
        } else {
            onError(R.string.unknown_exception);
        }
    }

    @Override
    public void onCamerasListChange() {
        cameras.clear();
        cameras.addAll(connectionManager.indiCameras.values());
        if (cameraSelectAdapter != null)
            cameraSelectAdapter.notifyDataSetChanged();
        if (cameraSelectSpinner != null)
            cameraSelectSpinner.setEnabled(!cameras.isEmpty());
        onCameraFunctionsChange();
    }

    @Override
    public void onConnectionLost() {
        selectedCameraDev = null;
        cameras.clear();
        if (cameraSelectAdapter != null)
            cameraSelectAdapter.notifyDataSetChanged();
        if (cameraSelectSpinner != null)
            cameraSelectSpinner.setEnabled(false);
        onCameraFunctionsChange();
    }

    private static class SwitchPropFilter extends Filter {

        private final SwitchPropertyAdapter adapter;

        SwitchPropFilter(SwitchPropertyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final ArrayList<INDISwitchElement> filtered = new ArrayList<>();
            FilterResults results = new FilterResults();
            if (constraint == null) {
                filtered.addAll(Arrays.asList(adapter.array));
            } else {
                String filterString = constraint.toString().trim().toLowerCase();
                for (INDISwitchElement el : adapter.array) {
                    if (el.getLabel().toLowerCase().contains(filterString)) filtered.add(el);
                }
            }
            results.values = filtered;
            results.count = filtered.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.list.clear();
            if (results.values != null)
                adapter.list.addAll((List<INDISwitchElement>) results.values);
            adapter.notifyDataSetChanged();
        }
    }

    private class CamerasArrayAdapter extends SimpleAdapter {

        CamerasArrayAdapter() {
            super(inflater);
        }

        @Override
        public int getCount() {
            return cameras.size();
        }

        @Override
        public INDICamera getItem(int position) {
            return cameras.get(position);
        }

        @Override
        public long getItemId(int position) {
            return cameras.get(position).hashCode();
        }

        @Override
        protected String getStringAt(int position) {
            return cameras.get(position).toString();
        }
    }

    private class SwitchPropertyAdapter extends SimpleAdapter implements Filterable {

        private final INDISwitchElement[] array;
        protected final ArrayList<INDISwitchElement> list = new ArrayList<>();

        SwitchPropertyAdapter(INDISwitchElement[] array) {
            super(inflater);
            this.array = array;
            Collections.addAll(list, array);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).hashCode();
        }

        @Override
        protected String getStringAt(int position) {
            return list.get(position).getLabel();
        }

        @Override
        public Filter getFilter() {
            return new SwitchPropFilter(this);
        }
    }

    private class SwitchPropertyStringAdapter extends SwitchPropertyAdapter {

        SwitchPropertyStringAdapter(INDISwitchElement[] array) {
            super(array);
        }

        @Override
        public String getItem(int position) {
            return list.get(position).getLabel();
        }
    }

    private class BinningValuesAdapter extends SimpleAdapter {

        BinningValuesAdapter() {
            super(inflater);
        }

        @Override
        public int getCount() {
            INDICamera camera = getCamera();
            if (camera == null) return 0;
            return camera.hasBinning() ? ((int) camera.binningXE.getMax()) : 0;
        }

        @Override
        public Integer getItem(int position) {
            return position + 1;
        }

        @Override
        public long getItemId(int position) {
            return getStringAt(position).hashCode();
        }

        @Override
        protected String getStringAt(int position) {
            position++;
            return position + "x" + position;
        }
    }

    private class SaveModeAdapter extends SimpleAdapter {

        private final INDICamera.SaveMode[] values = INDICamera.SaveMode.values();

        SaveModeAdapter() {
            super(inflater);
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public INDICamera.SaveMode getItem(int position) {
            return values[position];
        }

        @Override
        public long getItemId(int position) {
            return values[position].ordinal();
        }

        @Override
        protected String getStringAt(int position) {
            return values[position].toString(context);
        }
    }
}