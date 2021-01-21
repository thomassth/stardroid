package io.github.marcocipriani01.telescopetouch.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.util.Pair;

import com.github.chrisbanes.photoview.PhotoView;

import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.ActionFragment;
import io.github.marcocipriani01.telescopetouch.indi.AsyncBlobLoader;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class BLOBViewerFragment extends ActionFragment implements
        INDIServerConnectionListener, INDIDeviceListener, INDIPropertyListener, AsyncBlobLoader.LoadListener {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AsyncBlobLoader blobLoader = new AsyncBlobLoader(handler);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        blobLoader.setListener(this);
        View rootView = inflater.inflate(R.layout.fragment_blob_viewer, container, false);
        blobsEnableSwitch = rootView.findViewById(R.id.switch_image_receive);
        fitsStretchSwitch = rootView.findViewById(R.id.switch_image_stretch);
        fileSizeText = rootView.findViewById(R.id.blob_size);
        dimensionsText = rootView.findViewById(R.id.blob_dimensions);
        formatText = rootView.findViewById(R.id.blob_format);
        bppText = rootView.findViewById(R.id.blob_bpp);
        errorText = rootView.findViewById(R.id.blob_error_label);
        blobViewer = rootView.findViewById(R.id.blob_viewer);
        progressBar = rootView.findViewById(R.id.blob_loading);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectionManager.addINDIListener(this);
        connectionManager.setBlobEnabled(true); //TODO
        if (connectionManager.isConnected()) {
            List<INDIDevice> list = connectionManager.getIndiConnection().getDevicesAsList();
            for (INDIDevice device : list) {
                device.addINDIDeviceListener(this);
                List<INDIProperty<?>> properties = device.getPropertiesAsList();
                for (INDIProperty<?> property : properties) {
                    newProperty(device, property);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.removeINDIListener(this); //TODO
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
            String msg;
            if (bitmap != null) {
                try {
                    saveImage(context, bitmap, context.getString(R.string.app_name), new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
                    msg = "Saved!";
                } catch (Exception e) {
                    msg = "Saving error!";
                }
            } else {
                msg = "Nothing to save";
            }
            bitmapSaved = true;
            handler.post(() -> {
                notifyActionChange();
                requestActionSnack(R.string.antares);
            });
        }).start();
    }

    @Override
    public void onBitmapLoaded(Pair<Bitmap, String[]> result) {
        recycleBitmap();
        setBlobInfo(result.second);
        if (result.first == null) {
            errorText.setText("Unsupported format.");
            blobViewer.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        } else {
            bitmap = result.first;
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
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void loadBlob(INDIBLOBProperty property) {
        int elementCount = property.getElementCount();
        if (elementCount == 0) {
            errorText.setText("Empty property.");
            clearBitmap();
            notifyActionChange();
        } else if (elementCount == 1) {
            progressBar.setVisibility(View.VISIBLE);
            blobLoader.loadBitmap(property.getElementsAsList().get(0), fitsStretchSwitch.isChecked());
        } else {
            errorText.setText("Unsupported multi-image property.");
            clearBitmap();
            notifyActionChange();
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
            fileSizeText.setText("unknown");
            dimensionsText.setText("unknown");
            formatText.setText("unknown");
            bppText.setText("unknown");
        } else if (info.length != 4) {
            throw new IllegalArgumentException();
        } else {
            fileSizeText.setText((info[0] == null) ? "unknown" : info[0]);
            dimensionsText.setText((info[1] == null) ? "unknown" : info[1]);
            formatText.setText((info[2] == null) ? "unknown" : info[2]);
            bppText.setText((info[3] == null) ? "unknown" : info[3]);
        }
    }

    @SuppressWarnings("deprecation")
    private void saveImage(final Context context, final Bitmap bitmap, @NonNull String folderName, @NonNull String fileName) throws IOException {
        OutputStream outputStream;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + folderName);
            outputStream = resolver.openOutputStream(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues));
        } else {
            String imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                    File.separator + folderName;
            File file = new File(imageDirectory);
            if (!file.exists()) file.mkdir();
            outputStream = new FileOutputStream(new File(imageDirectory, fileName + ".jpg"));
            MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null, null);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
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
    public void newMessage(INDIServerConnection connection, Date date, String s) {

    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        if (property instanceof INDIBLOBProperty)
            property.addINDIPropertyListener(this);
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        if (property instanceof INDIBLOBProperty)
            property.removeINDIPropertyListener(this);
    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        if (property instanceof INDIBLOBProperty) {
            handler.post(() -> loadBlob((INDIBLOBProperty) property));
        }
    }

    @Override
    public void messageChanged(INDIDevice indiDevice) {

    }
}