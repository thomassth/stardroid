package io.github.marcocipriani01.telescopetouch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class Compass implements SensorEventListener {

    private static final String TAG = "Compass";
    private final SensorManager sensorManager;
    private final Sensor gSensor;
    private final Sensor sensor;
    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    public ImageView arrowView = null;
    private float azimuth = 0f;
    private float correctAzimuth = 0;

    public Compass(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start() {
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    private void adjustArrow() {
        if (arrowView == null) {
            Log.e(TAG, "Arrow view is not set");
            return;
        }
        Log.e(TAG, "Will set rotation from " + correctAzimuth + " to " + azimuth);
        Animation an = new RotateAnimation(-correctAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        correctAzimuth = azimuth;
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        arrowView.startAnimation(an);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            int type = event.sensor.getType();
            if (type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
            }
            float[] r = new float[9], i = new float[9];
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(r, orientation);
                //Log.e(TAG, "azimuth (rad): " + azimuth);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;
                //Log.e(TAG, "azimuth (deg): " + azimuth);
                adjustArrow();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}