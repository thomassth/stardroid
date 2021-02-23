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

import io.github.marcocipriani01.livephotoview.PhotoView;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.indi.AsyncBLOBLoader;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class FlyingBLOBViewActivity extends AppCompatActivity implements AsyncBLOBLoader.BLOBListener {

    @SuppressLint("StaticFieldLeak")
    private static FlyingBLOBViewActivity instance;
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
        pip();
        setContentView(R.layout.activity_pip_blob);
        darkerModeManager = new DarkerModeManager(this, null, PreferenceManager.getDefaultSharedPreferences(this));
        photoView = findViewById(R.id.pip_blob_view);
        photoView.setMaximumScale(20f);
        photoView.setImageBitmap(connectionManager.blobLoader.getLastBitmap());
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
            Bitmap bitmap = connectionManager.blobLoader.getLastBitmap();
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
        connectionManager.blobLoader.addListener(this);
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
        connectionManager.blobLoader.removeListener(this);
        instance = null;
    }

    @Override
    public void onBLOBLoading() {

    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, String[] metadata) {
        if (photoView != null) photoView.setImageBitmap(bitmap);
    }

    @Override
    public void onBitmapDestroy() {
        if (photoView != null) photoView.setImageBitmap(null);
    }

    @Override
    public void onBLOBException(Throwable e) {
        if (photoView != null) photoView.setImageBitmap(null);
    }
}