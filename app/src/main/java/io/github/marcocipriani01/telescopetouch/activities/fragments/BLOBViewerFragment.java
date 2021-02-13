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
import androidx.preference.PreferenceManager;

import com.github.chrisbanes.photoview.PhotoView;

import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.ProUtils;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.indi.AsyncBlobLoader;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class BLOBViewerFragment extends ActionFragment implements INDIServerConnectionListener,
        INDIDeviceListener, INDIPropertyListener, AsyncBlobLoader.LoadListener,
        CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = TelescopeTouchApp.getTag(BLOBViewerFragment.class);
    private static int selectedProperty = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AsyncBlobLoader blobLoader = new AsyncBlobLoader(handler);
    private final List<INDIBLOBProperty> properties = Collections.synchronizedList(new ArrayList<>());
    private SharedPreferences preferences;
    private Bitmap bitmap = null;
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
    private AppCompatSpinner selectionSpinner;
    private BLOBArrayAdapter selectionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blob_viewer, container, false);
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
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        blobLoader.setListener(this);
        boolean blobEnabled = preferences.getBoolean(ApplicationConstants.RECEIVE_BLOB_PREF, false);
        blobsEnableSwitch.setOnCheckedChangeListener(null);
        connectionManager.setBlobEnabled(blobEnabled);
        blobsEnableSwitch.setChecked(blobEnabled);
        blobsEnableSwitch.setSelected(blobEnabled);
        blobsEnableSwitch.setOnCheckedChangeListener(this);

        boolean stretchEnabled = preferences.getBoolean(ApplicationConstants.STRETCH_FITS_PREF, false);
        blobLoader.setStretch(stretchEnabled);
        fitsStretchSwitch.setOnCheckedChangeListener(null);
        fitsStretchSwitch.setChecked(stretchEnabled);
        fitsStretchSwitch.setSelected(stretchEnabled);
        fitsStretchSwitch.setOnCheckedChangeListener(this);

        connectionManager.addINDIListener(this);
        if (connectionManager.isConnected()) {
            List<INDIDevice> list = connectionManager.getIndiConnection().getDevicesAsList();
            for (INDIDevice device : list) {
                device.addINDIDeviceListener(this);
                List<INDIProperty<?>> properties = device.getPropertiesAsList();
                for (INDIProperty<?> property : properties) {
                    newProperty(device, property);
                }
            }
            if (properties.isEmpty())
                selectionSpinner.setEnabled(false);
        } else {
            selectionSpinner.setEnabled(false);
        }

        selectionSpinner.setOnItemSelectedListener(null);
        if (properties.size() > selectedProperty)
            selectionSpinner.setSelection(selectedProperty);
        selectionSpinner.setOnItemSelectedListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.removeINDIListener(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        blobLoader.setListener(null);
    }

    @Override
    public boolean isActionEnabled() {
        return (bitmap != null) && (!bitmapSaved);
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
                    final Uri uri = saveImage(context, bitmap, context.getString(R.string.app_name),
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
    public void onBitmapLoaded(Bitmap bitmap, String[] metadata) {
        recycleBitmap();
        setBlobInfo(metadata);
        if (bitmap == null) {
            errorText.setText(R.string.unsupported_format);
            blobViewer.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        } else {
            this.bitmap = bitmap;
            blobViewer.setImageBitmap(bitmap);
            blobViewer.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
            bitmapSaved = false;
        }
        progressBar.setVisibility(View.INVISIBLE);
        notifyActionChange();
    }

    @Override
    public void onBlobException(Throwable e) {
        Log.e("BLOBViewer", e.getLocalizedMessage(), e);
        if (e instanceof Error) {
            errorText.setText(R.string.out_of_memory);
        } else if (e instanceof FileNotFoundException) {
            errorText.setText(R.string.no_incoming_data);
        } else if (e instanceof EOFException) {
            errorText.setText(R.string.truncated_file);
        } else if (e instanceof IndexOutOfBoundsException) {
            errorText.setText(R.string.unsupported_color_fits);
        } else if (e instanceof UnsupportedOperationException) {
            errorText.setText(R.string.unsupported_bit_depth);
        } else if (e instanceof IllegalStateException) {
            errorText.setText(R.string.invalid_fits_image);
        } else {
            errorText.setText(R.string.unknown_exception);
        }
        clearBitmap();
        notifyActionChange();
    }

    private void loadBlob(INDIBLOBProperty property) {
        if ((errorText == null) || (progressBar == null) || (blobViewer == null)) return;
        switch (property.getElementCount()) {
            case 0:
                errorText.setText(R.string.empty_property);
                clearBitmap();
                notifyActionChange();
                break;
            case 1:
                if (blobLoader.queue(property.getElementsAsList().get(0)))
                    progressBar.setVisibility(View.VISIBLE);
                break;
            default:
                errorText.setText(R.string.error_multi_image_prop);
                clearBitmap();
                notifyActionChange();
                break;
        }
    }

    private void clearBitmap() {
        setBlobInfo(null);
        blobViewer.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.VISIBLE);
        recycleBitmap();
    }

    private void recycleBitmap() {
        blobViewer.setImageDrawable(null);
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
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

    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
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
                blobLoader.setStretch(isChecked);
                preferences.edit().putBoolean(ApplicationConstants.STRETCH_FITS_PREF, isChecked).apply();
                INDIBLOBProperty selectedItem = (INDIBLOBProperty) selectionSpinner.getSelectedItem();
                if (selectedItem != null)
                    loadBlob(selectedItem);
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedProperty = position;
        loadBlob(selectionAdapter.getItem(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        device.addINDIDeviceListener(this);
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        device.removeINDIDeviceListener(this);
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {

    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        if (property instanceof INDIBLOBProperty) {
            property.addINDIPropertyListener(this);
            handler.post(() -> {
                if (!properties.contains(property)) {
                    properties.add((INDIBLOBProperty) property);
                    selectionAdapter.notifyDataSetChanged();
                }
                selectionSpinner.setEnabled(true);
            });
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        if (property instanceof INDIBLOBProperty) {
            property.removeINDIPropertyListener(this);
            handler.post(() -> {
                properties.remove(property);
                if (selectionAdapter != null)
                    selectionAdapter.notifyDataSetChanged();
                if (properties.isEmpty() && (selectionSpinner != null))
                    selectionSpinner.setEnabled(false);
            });
        }
    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        if ((property instanceof INDIBLOBProperty) && (selectionSpinner != null) &&
                (selectionSpinner.getSelectedItem() == property))
            handler.post(() -> loadBlob((INDIBLOBProperty) property));
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date date, String s) {

    }

    @Override
    public void messageChanged(INDIDevice indiDevice) {

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