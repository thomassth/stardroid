package io.github.marcocipriani01.telescopetouch.control;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Sets the direction of view from the orientation sensors.
 *
 * @author John Taylor
 */
public class SensorOrientationController extends AbstractController implements SensorEventListener {

    private final static String TAG = TelescopeTouchApp.getTag(SensorOrientationController.class);
    private final SensorManager manager;
    private final Sensor rotationSensor;
    private final Sensor geomagneticRotationSensor;
    private final SharedPreferences sharedPreferences;

    @Inject
    SensorOrientationController(SensorManager manager, SharedPreferences sharedPreferences) {
        this.manager = manager;
        this.rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.geomagneticRotationSensor = manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void start() {
        if (manager != null) {
            if (sharedPreferences.getBoolean(ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO, false)) {
                Log.d(TAG, "Using geomagnetic rotation sensor");
                manager.registerListener(this, geomagneticRotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.d(TAG, "Using gyroscope sensor");
                manager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
        Log.d(TAG, "Registered sensor listener");
    }

    @Override
    public void stop() {
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ROTATION_VECTOR) {
            model.setPhoneSensorValues(event.values);
        } else if (type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            model.setPhoneSensorValues(event.values);
        } else {
            Log.e(TAG, "Unknown Sensor readings");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore
    }
}