package io.github.marcocipriani01.telescopetouch.util;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.units.Vector3;

/**
 * Adapts sensor output for use with the astronomer model.
 *
 * @author John Taylor
 */
public class PlainSmootherModelAdaptor implements SensorEventListener {

    private static final String TAG = TelescopeTouchApplication.getTag(PlainSmootherModelAdaptor.class);
    private final Vector3 magneticValues = ApplicationConstants.INITIAL_SOUTH.copy();
    private final Vector3 acceleration = ApplicationConstants.INITIAL_DOWN.copy();
    private final AstronomerModel model;
    private final boolean reverseMagneticZaxis;

    @Inject
    PlainSmootherModelAdaptor(AstronomerModel model, SharedPreferences sharedPreferences) {
        this.model = model;
        reverseMagneticZaxis = sharedPreferences.getBoolean(
                ApplicationConstants.REVERSE_MAGNETIC_Z_PREFKEY, false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleration.x = event.values[0];
            acceleration.y = event.values[1];
            acceleration.z = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues.x = event.values[0];
            magneticValues.y = event.values[1];
            // The z direction for the mag magneticField sensor is in the opposite
            // direction to that for accelerometer, except on some phones that are doing it wrong.
            // Yes that's right, the right thing to do is to invert it.  So if we reverse that,
            // we don't invert it.  Got it?
            // TODO(johntaylor): this might not be the best place to do this.
            magneticValues.z = reverseMagneticZaxis ? event.values[2] : -event.values[2];
        } else {
            Log.e(TAG, "Pump is receiving values that aren't accel or magnetic");
        }
        model.setPhoneSensorValues(acceleration, magneticValues);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}