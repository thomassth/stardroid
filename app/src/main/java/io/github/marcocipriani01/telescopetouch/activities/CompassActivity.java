package io.github.marcocipriani01.telescopetouch.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import io.github.marcocipriani01.telescopetouch.Compass;
import io.github.marcocipriani01.telescopetouch.R;

public class CompassActivity extends AppCompatActivity {

    private static final String TAG = "CompassActivity";
    private Compass compass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        compass = new Compass(this);
        compass.arrowView = (ImageView) findViewById(R.id.compass_arrow_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "Compass started");
        compass.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "Compass stopped");
        compass.stop();
    }
}