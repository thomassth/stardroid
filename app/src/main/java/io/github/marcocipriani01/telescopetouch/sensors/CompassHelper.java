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

package io.github.marcocipriani01.telescopetouch.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Display;

import androidx.preference.PreferenceManager;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;

public abstract class CompassHelper extends LocationHelper implements SensorEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;
    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    private final Display display;
    private final SharedPreferences preferences;
    private final SensorAccuracyMonitor sensorAccuracyMonitor;
    private boolean enableDeclination = true;
    private float magneticDeclination = 0.0f;

    public CompassHelper(Context context) {
        super(context);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        display = context.getDisplay();
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sensorAccuracyMonitor = new SensorAccuracyMonitor(sensorManager, context, preferences);
    }

    @Override
    protected void onLocationOk(Location location) {
        magneticDeclination = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(),
                (float) location.getAltitude(), System.currentTimeMillis()).getDeclination();
        onLocationAndDeclination(location, magneticDeclination);
    }

    @Override
    public boolean start() {
        preferences.registerOnSharedPreferenceChangeListener(this);
        enableDeclination = preferences.getBoolean(ApplicationConstants.MAGNETIC_DECLINATION_PREF, true);
        if (enableDeclination)
            super.start();
        onDeclinationEnabledChange(enableDeclination);
        sensorAccuracyMonitor.start();
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME) &&
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void stop() {
        super.stop();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        sensorManager.unregisterListener(this);
        sensorAccuracyMonitor.stop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.90f;
        synchronized (this) {
            int type = event.sensor.getType();
            if (type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1.0f - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1.0f - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1.0f - alpha) * event.values[2];
            } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1.0f - alpha) * event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1.0f - alpha) * event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1.0f - alpha) * event.values[2];
            }
            float[] rotation = new float[9];
            if (SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotation, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                if (enableDeclination)
                    azimuth += magneticDeclination;
                float displayRotation = display.getRotation() * 90.0f;
                onAzimuth((360.0f - azimuth - displayRotation) % 360.0f, -((azimuth + displayRotation) % 360.0f));
            }
        }
    }

    protected abstract void onLocationAndDeclination(Location location, float magneticDeclination);

    protected abstract void onAzimuth(float azimuth, float arrowRotation);

    protected abstract void onDeclinationEnabledChange(boolean show);

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (ApplicationConstants.MAGNETIC_DECLINATION_PREF.equals(key)) {
            enableDeclination = sharedPreferences.getBoolean(ApplicationConstants.MAGNETIC_DECLINATION_PREF, true);
            if (enableDeclination)
                super.start();
            onDeclinationEnabledChange(enableDeclination);
        }
    }
}