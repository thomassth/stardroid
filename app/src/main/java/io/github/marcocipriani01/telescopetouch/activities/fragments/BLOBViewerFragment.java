/*
 * Copyright (C) 2020  Marco Cipriani (@marcocipriani01)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import org.indilib.i4j.client.INDIBLOBProperty;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.marcocipriani01.livephotoview.PhotoView;
import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.ProUtils;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.FlyingBLOBViewActivity;
import io.github.marcocipriani01.telescopetouch.activities.util.ImprovedSpinnerListener;
import io.github.marcocipriani01.telescopetouch.indi.AsyncBLOBLoader;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class BLOBViewerFragment extends ActionFragment implements AsyncBLOBLoader.BLOBListener,
        CompoundButton.OnCheckedChangeListener, SharedPreferences.OnSharedPreferenceChangeListener,
        Toolbar.OnMenuItemClickListener, ConnectionManager.ManagerListener {

    private static final String TAG = TelescopeTouchApp.getTag(BLOBViewerFragment.class);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<INDIBLOBProperty> properties = new ArrayList<>();
    private SharedPreferences preferences;
    private boolean bitmapSaved = false;
    private SwitchCompat blobsEnableSwitch;
    private SwitchCompat fitsStretchSwitch;
    private TextView fileSizeText;
    private TextView dimensionsText;
    private TextView formatText;
    private TextView bppText;
    private TextView errorText;
    private PhotoView blobViewer;
    private ProgressBar progressBar;
    private final ImprovedSpinnerListener spinnerListener = new ImprovedSpinnerListener() {
        @Override
        protected void onImprovedItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if ((errorText == null) || (progressBar == null) || (blobViewer == null)) return;
            load(properties.get(pos));
        }
    };
    private AppCompatSpinner selectionSpinner;
    private BLOBArrayAdapter selectionAdapter;
    private boolean pipSupported = false;
    private MenuItem pipMenuItem = null;

    private void load(INDIBLOBProperty property) {
        if (property == null) {
            onError(R.string.empty_property);
            notifyActionChange();
        }
        switch (property.getElementCount()) {
            case 0:
                onError(R.string.empty_property);
                notifyActionChange();
                break;
            case 1:
                connectionManager.blobLoader.attach(property, property.getElementsAsList().get(0));
                break;
            default:
                onError(R.string.error_multi_image_prop);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blob_viewer, container, false);
        pipSupported = FlyingBLOBViewActivity.isSupported(context);
        if (pipSupported) setHasOptionsMenu(true);
        blobsEnableSwitch = rootView.findViewById(R.id.switch_image_receive);
        fitsStretchSwitch = rootView.findViewById(R.id.switch_image_stretch);
        fileSizeText = rootView.findViewById(R.id.blob_size);
        dimensionsText = rootView.findViewById(R.id.blob_dimensions);
        formatText = rootView.findViewById(R.id.blob_format);
        bppText = rootView.findViewById(R.id.blob_bpp);
        errorText = rootView.findViewById(R.id.blob_error_label);
        blobViewer = rootView.findViewById(R.id.blob_viewer);
        blobViewer.setMaximumScale(20f);
        progressBar = rootView.findViewById(R.id.blob_loading);
        selectionAdapter = new BLOBArrayAdapter(context);
        selectionSpinner = rootView.findViewById(R.id.spinner_select_image_blob);
        selectionSpinner.setAdapter(selectionAdapter);
        spinnerListener.attach(selectionSpinner);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean blobEnabled = preferences.getBoolean(ApplicationConstants.RECEIVE_BLOB_PREF, false);
        blobsEnableSwitch.setOnCheckedChangeListener(null);
        connectionManager.setBlobEnabled(blobEnabled);
        blobsEnableSwitch.setChecked(blobEnabled);
        blobsEnableSwitch.setSelected(blobEnabled);
        blobsEnableSwitch.setOnCheckedChangeListener(this);

        boolean stretchEnabled = preferences.getBoolean(ApplicationConstants.STRETCH_FITS_PREF, false);
        connectionManager.blobLoader.setStretch(stretchEnabled);
        fitsStretchSwitch.setOnCheckedChangeListener(null);
        fitsStretchSwitch.setChecked(stretchEnabled);
        fitsStretchSwitch.setSelected(stretchEnabled);
        fitsStretchSwitch.setOnCheckedChangeListener(this);

        connectionManager.addManagerListener(this);
        properties.clear();
        if (connectionManager.isConnected()) {
            properties.addAll(connectionManager.getBlobProperties());
            selectionAdapter.notifyDataSetChanged();
            if (properties.isEmpty()) {
                selectionSpinner.setEnabled(false);
            } else {
                selectionSpinner.setEnabled(true);
                INDIBLOBProperty selected = connectionManager.blobLoader.getProp();
                if (selected == null) {
                    load(properties.get(0));
                } else {
                    int loaderPropIndex = properties.indexOf(selected);
                    if (loaderPropIndex == -1) {
                        connectionManager.blobLoader.detach();
                    } else {
                        selectionSpinner.setSelection(loaderPropIndex);
                    }
                }
                Bitmap lastBitmap = connectionManager.blobLoader.getLastBitmap();
                blobViewer.setImageBitmap(lastBitmap);
                if (lastBitmap == null) {
                    errorText.setText(R.string.no_incoming_data);
                    blobViewer.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    blobViewer.setVisibility(View.VISIBLE);
                    errorText.setVisibility(View.GONE);
                    bitmapSaved = false;
                }
            }
        } else {
            selectionAdapter.notifyDataSetChanged();
            selectionSpinner.setEnabled(false);
            errorText.setText(R.string.no_incoming_data);
            blobViewer.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.INVISIBLE);
        notifyActionChange();
        connectionManager.blobLoader.addListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.removeManagerListener(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        blobViewer.setImageBitmap(null);
        connectionManager.blobLoader.removeListener(this);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item == pipMenuItem) {
            if (ProUtils.isPro) {
                if (FlyingBLOBViewActivity.isVisible()) {
                    FlyingBLOBViewActivity.finishInstance();
                } else if (((AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE))
                        .checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(),
                                context.getPackageName()) != AppOpsManager.MODE_ALLOWED) {
                    requestActionSnack(R.string.pip_permission_required);
                } else {
                    startActivity(new Intent(context, FlyingBLOBViewActivity.class));
                }
            } else {
                requestActionSnack(R.string.pro_feature);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isActionEnabled() {
        return connectionManager.blobLoader.hasBitmap() && (!bitmapSaved);
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
                    final Uri uri = saveImage(context, connectionManager.blobLoader.getLastBitmap(),
                            context.getString(R.string.app_name),
                            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
                    bitmapSaved = true;
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

    @Override
    public void onBLOBLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, String[] metadata) {
        setBlobInfo(metadata);
        if (bitmap == null) {
            errorText.setText(R.string.unsupported_format);
            blobViewer.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        } else {
            blobViewer.setImageBitmap(bitmap);
            blobViewer.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
            bitmapSaved = false;
        }
        progressBar.setVisibility(View.INVISIBLE);
        notifyActionChange();
    }

    @Override
    public void onBitmapDestroy() {
        blobViewer.setImageBitmap(null);
        notifyActionChange();
    }

    @Override
    public void onBLOBException(Throwable e) {
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

    private void onError(int errorMsg) {
        setBlobInfo(null);
        errorText.setText(errorMsg);
        blobViewer.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.VISIBLE);
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

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private Uri saveImage(final Context context, final Bitmap bitmap, @NonNull String folderName, @NonNull String fileName) throws IOException {
        OutputStream stream;
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + folderName);
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            stream = resolver.openOutputStream(uri);
        } else {
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                    File.separator + folderName;
            File dirFile = new File(directory);
            if (!dirFile.exists()) dirFile.mkdir();
            File file = new File(directory, fileName + ".jpg");
            uri = Uri.fromFile(file);
            stream = new FileOutputStream(file);
            MediaScannerConnection.scanFile(context, new String[]{dirFile.toString()}, null, null);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.flush();
        stream.close();
        return uri;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == blobsEnableSwitch) {
            connectionManager.setBlobEnabled(isChecked);
            preferences.edit().putBoolean(ApplicationConstants.RECEIVE_BLOB_PREF, isChecked).apply();
        } else if (buttonView == fitsStretchSwitch) {
            if (ProUtils.isPro) {
                connectionManager.blobLoader.setStretch(isChecked);
                connectionManager.blobLoader.reload();
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(ApplicationConstants.RECEIVE_BLOB_PREF) && (blobsEnableSwitch != null)) {
            boolean blobEnabled = sharedPreferences.getBoolean(ApplicationConstants.RECEIVE_BLOB_PREF, false);
            blobsEnableSwitch.setOnCheckedChangeListener(null);
            blobsEnableSwitch.setChecked(blobEnabled);
            blobsEnableSwitch.setSelected(blobEnabled);
            blobsEnableSwitch.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onConnectionLost() {
        properties.clear();
        if (selectionAdapter != null)
            selectionAdapter.notifyDataSetChanged();
        if (selectionSpinner != null)
            selectionSpinner.setEnabled(false);
    }

    @Override
    public void onBLOBListChange() {
        properties.clear();
        properties.addAll(connectionManager.getBlobProperties());
        if (selectionAdapter != null)
            selectionAdapter.notifyDataSetChanged();
        if (selectionSpinner != null)
            selectionSpinner.setEnabled(!properties.isEmpty());
    }

    private static class ViewHolder {
        TextView name;
    }

    private class BLOBArrayAdapter extends BaseAdapter {

        private final LayoutInflater inflater;

        BLOBArrayAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return properties.size();
        }

        @Override
        public INDIBLOBProperty getItem(int position) {
            return properties.get(position);
        }

        @Override
        public long getItemId(int position) {
            return properties.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, R.layout.blob_spinner_item);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, R.layout.blob_spinner_dropdown_item);
        }

        @SuppressLint("SetTextI18n")
        private View createView(int position, View convertView, int resourceId) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(resourceId, null, false);
                holder.name = convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            INDIBLOBProperty property = properties.get(position);
            holder.name.setText(property.getName() + " @" + property.getDevice().getName());
            return convertView;
        }
    }
}