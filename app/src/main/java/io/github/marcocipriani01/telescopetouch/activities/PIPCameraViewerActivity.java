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

package io.github.marcocipriani01.telescopetouch.activities;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Collection;

import io.github.marcocipriani01.livephotoview.PhotoView;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;
import io.github.marcocipriani01.telescopetouch.indi.INDICamera;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class PIPCameraViewerActivity extends AppCompatActivity
        implements INDICamera.CameraListener, ConnectionManager.ManagerListener {

    public static final String INDI_CAMERA_EXTRA = "indi_camera";
    @SuppressLint("StaticFieldLeak")
    private static PIPCameraViewerActivity instance;
    private INDICamera camera;
    private PhotoView photoView;
    private DarkerModeManager darkerModeManager;

    public static boolean isVisible() {
        return instance != null;
    }

    public static boolean isSupported(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
        return false;
    }

    public static void finishInstance() {
        if (instance != null) instance.finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        camera = getIntent().getParcelableExtra(INDI_CAMERA_EXTRA);
        if (camera == null) {
            Toast.makeText(this, R.string.no_camera_available, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            pip();
            setContentView(R.layout.activity_pip_blob);
            darkerModeManager = new DarkerModeManager(this, null, PreferenceManager.getDefaultSharedPreferences(this));
            photoView = findViewById(R.id.pip_blob_view);
            photoView.setMaximumScale(20f);
            photoView.setImageBitmap(camera.getLastBitmap());
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isInPictureInPictureMode()) pip();
        }
    }

    private void pip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational ratio;
            Bitmap bitmap = camera.getLastBitmap();
            if (bitmap == null) {
                ratio = new Rational(1, 1);
            } else {
                ratio = new Rational(bitmap.getWidth(), bitmap.getHeight());
            }
            enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(ratio).build());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enterPictureInPictureMode();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (((AppOpsManager) getSystemService(Context.APP_OPS_SERVICE))
                .checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), getPackageName())
                != AppOpsManager.MODE_ALLOWED) {
            Toast.makeText(this, R.string.pip_permission_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        camera.addListener(this);
        connectionManager.addManagerListener(this);
        instance = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        darkerModeManager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        darkerModeManager.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.removeListener(this);
        connectionManager.removeManagerListener(this);
        instance = null;
    }

    @Override
    public void onImageLoaded(@Nullable Bitmap bitmap, String[] metadata) {
        if (photoView != null) photoView.setImageBitmap(bitmap);
    }

    @Override
    public void onImageLoadingError(Throwable e) {
        if (photoView != null) photoView.setImageBitmap(null);
    }

    @Override
    public void onBitmapDestroy() {
        if (photoView != null) photoView.setImageBitmap(null);
    }

    @Override
    public void onConnectionLost() {
        if (photoView != null) photoView.setImageBitmap(null);
        finishInstance();
    }

    @Override
    public void onCamerasListChange() {
        synchronized (connectionManager.indiCameras) {
            Collection<INDICamera> cameras = connectionManager.indiCameras.values();
            for (INDICamera camera : cameras) {
                if (camera == this.camera) return;
            }
        }
        finishInstance();
    }
}