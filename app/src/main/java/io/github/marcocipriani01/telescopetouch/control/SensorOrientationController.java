package io.github.marcocipriani01.telescopetouch.control;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Provider;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;
import io.github.marcocipriani01.telescopetouch.util.smoothers.ExponentiallyWeightedSmoother;
import io.github.marcocipriani01.telescopetouch.util.smoothers.PlainSmootherModelAdaptor;

/**
 * Sets the direction of view from the orientation sensors.
 *
 * @author John Taylor
 */
public class SensorOrientationController extends AbstractController
        implements SensorEventListener {
    // TODO(johntaylor): this class needs to be refactored to use the new
    // sensor API and to behave properly when sensors are not available.

    private final static String TAG = MiscUtil.getTag(SensorOrientationController.class);
    /**
     * Parameters that control the smoothing of the accelerometer and
     * magnetic sensors.
     */
    private static final SensorDampingSettings[] ACC_DAMPING_SETTINGS = {
            new SensorDampingSettings(0.7f, 3),
            new SensorDampingSettings(0.7f, 3),
            new SensorDampingSettings(0.1f, 3),
            new SensorDampingSettings(0.1f, 3),
    };
    private static final SensorDampingSettings[] MAG_DAMPING_SETTINGS = {
            new SensorDampingSettings(0.05f, 3),  // Derived for the Nexus One
            new SensorDampingSettings(0.001f, 4),  // Derived for the unpatched MyTouch Slide
            new SensorDampingSettings(0.0001f, 5),  // Just guessed for Nexus 6
            new SensorDampingSettings(0.000001f, 5)  // Just guessed for Nexus 6
    };
    private final SensorManager manager;
    private final Provider<PlainSmootherModelAdaptor> modelAdaptorProvider;
    private final Sensor rotationSensor;
    private final SharedPreferences sharedPreferences;
    private SensorEventListener accelerometerSmoother;
    private SensorEventListener compassSmoother;

    @Inject
    SensorOrientationController(Provider<PlainSmootherModelAdaptor> modelAdaptorProvider,
                                SensorManager manager, SharedPreferences sharedPreferences) {
        this.manager = manager;
        this.modelAdaptorProvider = modelAdaptorProvider;
        this.rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void start() {
        PlainSmootherModelAdaptor modelAdaptor = modelAdaptorProvider.get();

        if (manager != null) {
            if (!sharedPreferences.getBoolean(ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO,
                    false)) {
                Log.d(TAG, "Using rotation sensor");
                manager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            } else {
                // TODO(jontayler): remove this code once enough it's used in few enough phones.
                Log.d(TAG, "Using classic sensors");
                Log.d(TAG, "Exponentially weighted smoothers used");
                String dampingPreference = sharedPreferences.getString(
                        ApplicationConstants.SENSOR_DAMPING_PREF_KEY,
                        ApplicationConstants.SENSOR_DAMPING_STANDARD);
                String speedPreference = sharedPreferences.getString(ApplicationConstants.SENSOR_SPEED_PREF_KEY,
                        ApplicationConstants.SENSOR_SPEED_STANDARD);
                Log.d(TAG, "Sensor damping preference " + dampingPreference);
                Log.d(TAG, "Sensor speed preference " + speedPreference);
                int dampingIndex = 0;
                if (ApplicationConstants.SENSOR_DAMPING_HIGH.equals(dampingPreference)) {
                    dampingIndex = 1;
                } else if (ApplicationConstants.SENSOR_DAMPING_EXTRA_HIGH.equals(dampingPreference)) {
                    dampingIndex = 2;
                } else if (ApplicationConstants.SENSOR_DAMPING_REALLY_HIGH.equals(dampingPreference)) {
                    dampingIndex = 3;
                }
                int sensorSpeed = SensorManager.SENSOR_DELAY_GAME;
                if (ApplicationConstants.SENSOR_SPEED_SLOW.equals(speedPreference)) {
                    sensorSpeed = SensorManager.SENSOR_DELAY_NORMAL;
                } else if (ApplicationConstants.SENSOR_SPEED_HIGH.equals(speedPreference)) {
                    sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;
                }
                accelerometerSmoother = new ExponentiallyWeightedSmoother(
                        modelAdaptor,
                        ACC_DAMPING_SETTINGS[dampingIndex].damping,
                        ACC_DAMPING_SETTINGS[dampingIndex].exponent);
                compassSmoother = new ExponentiallyWeightedSmoother(
                        modelAdaptor,
                        MAG_DAMPING_SETTINGS[dampingIndex].damping,
                        MAG_DAMPING_SETTINGS[dampingIndex].exponent);
                manager.registerListener(accelerometerSmoother,
                        manager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER),
                        sensorSpeed);
                manager.registerListener(compassSmoother,
                        manager.getDefaultSensor(SensorManager.SENSOR_MAGNETIC_FIELD),
                        sensorSpeed);
            }
        }
        Log.d(TAG, "Registered sensor listener");
    }

    @Override
    public void stop() {
        Log.d(
                TAG, "Unregistering sensor listeners: " + accelerometerSmoother + ", "
                        + compassSmoother + ", " + this);
        manager.unregisterListener(accelerometerSmoother);
        manager.unregisterListener(compassSmoother);
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor != rotationSensor) {
            return;
        }
        model.setPhoneSensorValues(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore
    }

    private static class SensorDampingSettings {
        public float damping;
        public int exponent;

        public SensorDampingSettings(float damping, int exponent) {
            this.damping = damping;
            this.exponent = exponent;
        }
    }
}
