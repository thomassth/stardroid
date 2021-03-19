/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.activities.util.SensorAccuracyDecoder;

public class CompassCalibrationActivity extends AppCompatActivity implements SensorEventListener {

    public static final String HIDE_CHECKBOX = "hide checkbox";
    public static final String AUTO_DISMISSABLE = "auto dismissable";
    @Inject
    SensorManager sensorManager;
    @Inject
    SensorAccuracyDecoder accuracyDecoder;
    @Inject
    SharedPreferences preferences;
    private Sensor magneticSensor;
    private CheckBox checkBoxView;
    private boolean accuracyReceived = false;
    private DarkerModeManager darkerModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerCompassCalibrationComponent.builder()
                .applicationComponent(((TelescopeTouchApp) getApplication()).getApplicationComponent())
                .compassCalibrationModule(new CompassCalibrationModule(this)).build().inject(this);
        darkerModeManager = new DarkerModeManager(this, null, preferences);
        setTheme(darkerModeManager.getPref() ? R.style.DarkerAppTheme : R.style.AppTheme);
        setContentView(R.layout.activity_compass_calibration);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        this.<WebView>findViewById(R.id.compass_calib_activity_webview).loadUrl("file:///android_asset/html/how_to_calibrate.html");

        checkBoxView = findViewById(R.id.compass_calib_activity_donotshow);
        boolean hideCheckbox = getIntent().getBooleanExtra(HIDE_CHECKBOX, false);
        if (hideCheckbox) {
            checkBoxView.setVisibility(View.GONE);
            View reasonText = findViewById(R.id.compass_calib_activity_explain_why);
            reasonText.setVisibility(View.GONE);
        }
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticSensor == null) {
            ((TextView) findViewById(R.id.compass_calib_activity_compass_accuracy)).setText(
                    getString(R.string.sensor_absent));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (magneticSensor != null) {
            sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
        }
        darkerModeManager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (checkBoxView.isChecked()) {
            preferences.edit().putBoolean(ApplicationConstants.NO_SHOW_CALIBRATION_DIALOG_PREF, true).apply();
        }
        darkerModeManager.stop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!accuracyReceived) {
            onAccuracyChanged(event.sensor, event.accuracy);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        accuracyReceived = true;
        TextView accuracyTextView = findViewById(R.id.compass_calib_activity_compass_accuracy);
        String accuracyText = accuracyDecoder.getTextForAccuracy(accuracy);
        accuracyTextView.setText(accuracyText);
        accuracyTextView.setTextColor(accuracyDecoder.getColorForAccuracy(accuracy));
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH && getIntent().getBooleanExtra(AUTO_DISMISSABLE, false)) {
            Toast.makeText(this, R.string.sensor_accuracy_high, Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    public void onOkClicked(View unused) {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}