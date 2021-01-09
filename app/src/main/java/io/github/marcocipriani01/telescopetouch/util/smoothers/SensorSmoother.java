package io.github.marcocipriani01.telescopetouch.util.smoothers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


public abstract class SensorSmoother implements SensorEventListener {

    protected SensorEventListener listener;

    public SensorSmoother(SensorEventListener listener) {
        this.listener = listener;
    }

    public abstract void onSensorChanged(SensorEvent event);

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do Nothing
    }
}
