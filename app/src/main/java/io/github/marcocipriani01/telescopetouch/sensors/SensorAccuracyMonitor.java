/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.sensors;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.CompassCalibrationActivity;
import io.github.marcocipriani01.telescopetouch.activities.SkyMapActivity;
import io.github.marcocipriani01.telescopetouch.astronomy.TimeUtils;

/**
 * Monitors the compass accuracy and if it is not medium or high warns the user.
 * Created by johntaylor on 4/24/16.
 */
public class SensorAccuracyMonitor implements SensorEventListener {

    private static final String TAG = TelescopeTouchApp.getTag(SensorAccuracyMonitor.class);
    private static final long MIN_INTERVAL_BETWEEN_WARNINGS = 180 * TimeUtils.MILLISECONDS_PER_SECOND;
    private final SensorManager sensorManager;
    private final Sensor compassSensor;
    private final Activity activity;
    private final SharedPreferences sharedPreferences;
    private boolean started = false;
    private boolean hasReading = false;

    public SensorAccuracyMonitor(SensorManager sensorManager, Activity activity, SharedPreferences sharedPreferences) {
        Log.d(TAG, "Creating new accuracy monitor");
        this.sensorManager = sensorManager;
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Inject
    public SensorAccuracyMonitor(SensorManager sensorManager, SkyMapActivity activity, SharedPreferences sharedPreferences) {
        this(sensorManager, (Activity) activity, sharedPreferences);
    }

    /**
     * Starts monitoring.
     */
    public void start() {
        if (started) {
            return;
        }
        Log.d(TAG, "Starting monitoring compass accuracy");
        if (compassSensor != null) {
            sensorManager.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_UI);
        }
        started = true;
    }

    /**
     * Stops monitoring.  It's important this is called to disconnect from the sensors and
     * ensure the app does not needlessly consume power when in the background.
     */
    public void stop() {
        Log.d(TAG, "Stopping monitoring compass accuracy");
        started = false;
        hasReading = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!hasReading) {
            onAccuracyChanged(event.sensor, event.accuracy);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        hasReading = true;
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            return;  // OK
        }
        Log.d(TAG, "Compass accuracy insufficient");
        if ((System.currentTimeMillis() - sharedPreferences.getLong(ApplicationConstants.LAST_CALIBRATION_WARNING_PREF, 0)) < MIN_INTERVAL_BETWEEN_WARNINGS) {
            Log.d(TAG, "...but too soon to warn again");
            return;
        }
        sharedPreferences.edit().putLong(ApplicationConstants.LAST_CALIBRATION_WARNING_PREF, System.currentTimeMillis()).apply();
        if (sharedPreferences.getBoolean(ApplicationConstants.NO_SHOW_CALIBRATION_DIALOG_PREF, false)) {
            Snackbar.make(activity.getWindow().getDecorView().getRootView(),
                    R.string.inaccurate_compass_warning, Snackbar.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(activity, CompassCalibrationActivity.class);
            intent.putExtra(CompassCalibrationActivity.HIDE_CHECKBOX, false);
            intent.putExtra(CompassCalibrationActivity.AUTO_DISMISSABLE, true);
            activity.startActivity(intent);
        }
    }
}