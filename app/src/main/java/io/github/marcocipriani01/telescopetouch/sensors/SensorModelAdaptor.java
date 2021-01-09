package io.github.marcocipriani01.telescopetouch.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;

/**
 * Connects the rotation vector to the model code.
 */
public class SensorModelAdaptor implements SensorEventListener {
    private final AstronomerModel model;

    @Inject
    SensorModelAdaptor(AstronomerModel model) {
        this.model = model;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // do something with the model
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }
}
