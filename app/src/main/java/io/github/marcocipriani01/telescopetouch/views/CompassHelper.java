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

package io.github.marcocipriani01.telescopetouch.views;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class CompassHelper implements SensorEventListener {

    private static final String TAG = TelescopeTouchApp.getTag(CompassHelper.class);
    private final SensorManager sensorManager;
    private final Sensor gSensor;
    private final Sensor magneticFieldSensor;
    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    private final Display display;
    private ImageView arrowView = null;
    private float lastAzimuth = 0;

    public CompassHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        display = context.getDisplay();
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void setArrowView(ImageView arrowView) {
        this.arrowView = arrowView;
    }

    public void start() {
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;
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
            float[] r = new float[9];
            if (SensorManager.getRotationMatrix(r, null, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(r, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + (display.getRotation() * 90)) % 360;
                Log.d(TAG, "Azimuth= " + azimuth);
                Log.d(TAG, "Will set rotation from " + lastAzimuth + " to " + azimuth);
                Animation animation = new RotateAnimation(-lastAzimuth, -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                lastAzimuth = azimuth;
                animation.setDuration(500);
                animation.setRepeatCount(0);
                animation.setFillAfter(true);
                arrowView.startAnimation(animation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}