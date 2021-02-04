/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.activities.util.SensorAccuracyDecoder;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.control.LocationController;
import io.github.marcocipriani01.telescopetouch.control.MagneticDeclinationSwitcher;
import io.github.marcocipriani01.telescopetouch.control.Pointing;
import io.github.marcocipriani01.telescopetouch.maths.Formatters;

public class DiagnosticActivity extends InjectableActivity implements SensorEventListener {

    private static final String TAG = TelescopeTouchApp.getTag(DiagnosticActivity.class);
    private static final int UPDATE_PERIOD_MILLIS = 500;
    private final Set<Sensor> knownSensorAccuracies = new HashSet<>();
    @Inject
    TelescopeTouchApp app;
    @Inject
    SensorManager sensorManager;
    @Inject
    ConnectivityManager connectivityManager;
    @Inject
    LocationManager locationManager;
    @Inject
    LocationController locationController;
    @Inject
    AstronomerModel model;
    @Inject
    MagneticDeclinationSwitcher magneticSwitcher;
    @Inject
    Handler handler;
    @Inject
    SensorAccuracyDecoder sensorAccuracyDecoder;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gyroscope;
    private Sensor rotationVectorSensor;
    private Sensor lightSensor;
    private boolean continueUpdates;
    private DarkerModeManager darkerModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerDiagnosticActivityComponent.builder().applicationComponent(
                getApplicationComponent()).diagnosticActivityModule(new DiagnosticActivityModule(this))
                .build().inject(this);
        darkerModeManager = new DarkerModeManager(getWindow(), null, PreferenceManager.getDefaultSharedPreferences(this));
        setTheme(darkerModeManager.getPref() ? R.style.DarkerAppTheme : R.style.AppTheme);
        setContentView(R.layout.activity_diagnostic);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        magneticSwitcher.init();
    }

    @Override
    public void onStart() {
        super.onStart();
        setText(R.id.diagnose_phone_txt, Build.MODEL + " (" + Build.HARDWARE + ") " + Locale.getDefault().getLanguage());
        setText(R.id.diagnose_android_version_txt, String.format(Build.VERSION.RELEASE + " (%d)", Build.VERSION.SDK_INT));
        setText(R.id.diagnose_skymap_version_txt, String.format(app.getVersionName() + " (%d)", app.getVersion()));
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeSensors();
        continueUpdates = true;
        handler.post(new Runnable() {
            public void run() {
                updateLocation();
                updateModel();
                updateNetwork();
                if (continueUpdates) {
                    handler.postDelayed(this, UPDATE_PERIOD_MILLIS);
                }
            }
        });
        darkerModeManager.start();
        locationController.start();
    }

    private void onResumeSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        int absentSensorColor = getResources().getColor(R.color.absent_sensor);
        if (accelerometer == null) {
            setColor(R.id.diagnose_accelerometer_values_txt, absentSensorColor);
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            setColor(R.id.diagnose_compass_values_txt, absentSensorColor);
        } else {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope == null) {
            setColor(R.id.diagnose_gyro_values_txt, absentSensorColor);
        } else {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor == null) {
            setColor(R.id.diagnose_rotation_values_txt, absentSensorColor);
        } else {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            setColor(R.id.diagnose_light_values_txt, absentSensorColor);
        } else {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void updateLocation() {
        // TODO(johntaylor): add other things like number of satellites and status
        String gpsStatusMessage;
        try {
            LocationProvider gps = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gps == null) {
                gpsStatusMessage = getString(R.string.no_gps);
            } else {
                gpsStatusMessage = gpsStatus ? getString(R.string.enabled) : getString(R.string.disabled);
            }
        } catch (SecurityException ex) {
            gpsStatusMessage = getString(R.string.permission_disabled);
        }
        setText(R.id.diagnose_gps_status_txt, gpsStatusMessage);
        Location location = locationController.getCurrentLocation();
        setText(R.id.diagnose_location_txt, Formatters.latitudeToString(location.getLatitude(), this) + ", " + Formatters.longitudeToString(location.getLongitude(), this));
    }

    private void updateModel() {
        float magCorrection = model.getMagneticCorrection();
        setText(R.id.diagnose_magnetic_correction_txt, Formatters.magDeclinationToString(magCorrection, this));
        Pointing pointing = model.getPointing();
        GeocentricCoordinates lineOfSight = pointing.getLineOfSight();
        setText(R.id.diagnose_pointing_txt, lineOfSight.getRa() + ", " + lineOfSight.getDec());
        Date time = model.getTime().getTime();
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
        setText(R.id.diagnose_local_datetime_txt, dateFormat.format(time) + " " + timeFormat.format(time));
        TimeZone utc = TimeZone.getTimeZone("UTC");
        dateFormat.setTimeZone(utc);
        timeFormat.setTimeZone(utc);
        setText(R.id.diagnose_utc_datetime_txt, dateFormat.format(time) + " " + timeFormat.format(time));
    }

    @Override
    public void onPause() {
        super.onPause();
        continueUpdates = false;
        sensorManager.unregisterListener(this);
        darkerModeManager.stop();
        locationController.stop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        knownSensorAccuracies.add(sensor);
        Log.d(TAG, "set size" + knownSensorAccuracies.size());
        int sensorViewId;
        if (sensor == accelerometer) {
            sensorViewId = R.id.diagnose_accelerometer_values_txt;
        } else if (sensor == magnetometer) {
            sensorViewId = R.id.diagnose_compass_values_txt;
        } else if (sensor == gyroscope) {
            sensorViewId = R.id.diagnose_gyro_values_txt;
        } else if (sensor == rotationVectorSensor) {
            sensorViewId = R.id.diagnose_rotation_values_txt;
        } else if (sensor == lightSensor) {
            sensorViewId = R.id.diagnose_light_values_txt;
        } else {
            Log.e(TAG, "Receiving accuracy change for unknown sensor " + sensor);
            return;
        }
        setColor(sensorViewId, sensorAccuracyDecoder.getColorForAccuracy(accuracy));
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (!knownSensorAccuracies.contains(sensor)) {
            onAccuracyChanged(sensor, event.accuracy);
        }
        int valuesViewId;
        if (sensor == accelerometer) {
            valuesViewId = R.id.diagnose_accelerometer_values_txt;
        } else if (sensor == magnetometer) {
            valuesViewId = R.id.diagnose_compass_values_txt;
        } else if (sensor == gyroscope) {
            valuesViewId = R.id.diagnose_gyro_values_txt;
        } else if (sensor == rotationVectorSensor) {
            valuesViewId = R.id.diagnose_rotation_values_txt;
        } else if (sensor == lightSensor) {
            valuesViewId = R.id.diagnose_light_values_txt;
        } else {
            Log.e(TAG, "Receiving values for unknown sensor " + sensor);
            return;
        }
        float[] values = event.values;
        setArrayValuesInUi(valuesViewId, values);

        // Something special for rotation sensor - convert to a matrix.
        if (sensor == rotationVectorSensor) {
            float[] matrix = new float[9];
            SensorManager.getRotationMatrixFromVector(matrix, event.values);
            for (int row = 0; row < 3; ++row) {
                switch (row) {
                    case 0:
                        valuesViewId = R.id.diagnose_rotation_matrix_row1_txt;
                        break;
                    case 1:
                        valuesViewId = R.id.diagnose_rotation_matrix_row2_txt;
                        break;
                    case 2:
                    default:
                        valuesViewId = R.id.diagnose_rotation_matrix_row3_txt;
                }
                float[] rowValues = new float[3];
                System.arraycopy(matrix, row * 3, rowValues, 0, 3);
                setArrayValuesInUi(valuesViewId, rowValues);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void setArrayValuesInUi(int valuesViewId, float[] values) {
        StringBuilder builder = new StringBuilder();
        for (float value : values) {
            builder.append(String.format("%.2f", value));
            builder.append(", ");
        }
        builder.setLength(builder.length() - 2);
        setText(valuesViewId, builder.toString());
    }

    @SuppressWarnings("deprecation")
    private void updateNetwork() {
        String message;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Network network = connectivityManager.getActiveNetwork();
            boolean isConnected = (network != null);
            message = isConnected ? getString(R.string.connected) : getString(R.string.disconnected);
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (isConnected) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    message += getString(R.string.wifi);
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    message += getString(R.string.cell_network);
                }
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
            message = isConnected ? getString(R.string.connected) : getString(R.string.disconnected);
            if (isConnected) {
                int type = activeNetwork.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    message += getString(R.string.wifi);
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    message += getString(R.string.cell_network);
                }
            }
        }
        setText(R.id.diagnose_network_status_txt, message);
    }

    private void setText(int viewId, String text) {
        ((TextView) findViewById(viewId)).setText(text);
    }

    private void setColor(int viewId, int color) {
        ((TextView) findViewById(viewId)).setTextColor(color);
    }
}