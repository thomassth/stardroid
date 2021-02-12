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

package io.github.marcocipriani01.telescopetouch.sensors;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public abstract class LocationHelper implements LocationListener {

    private static final String TAG = TelescopeTouchApp.getTag(LocationHelper.class);
    private static final int MINIMUM_DISTANCE_UPDATES_METRES = 2000;
    private static final int LOCATION_UPDATE_TIME_MS = 180000;
    private final Activity activity;
    private final View rootView;
    private final LocationManager locationManager;
    private final SharedPreferences preferences;

    public LocationHelper(Activity activity, LocationManager locationManager) {
        this.activity = activity;
        rootView = activity.getWindow().getDecorView().getRootView();
        this.locationManager = locationManager;
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public LocationHelper(Activity activity) {
        this(activity, ContextCompat.getSystemService(activity, LocationManager.class));
    }

    public boolean start() {
        Log.d(TAG, "Start");
        if (preferences.getBoolean(ApplicationConstants.NO_AUTO_LOCATE_PREF, false)) {
            Log.d(TAG, "User has elected to set location manually.");
            setLocationFromPrefs();
            return true;
        }
        try {
            if (locationManager == null) {
                Log.e(TAG, "Location manager was null - using preferences");
                setLocationFromPrefs();
                return true;
            }
            Criteria locationCriteria = getLocationCriteria();
            String locationProvider = locationManager.getBestProvider(locationCriteria, true);
            if (locationProvider == null) {
                Log.w(TAG, "No location provider is enabled");
                if (locationManager.getBestProvider(locationCriteria, false) == null) {
                    Log.i(TAG, "No location provider is even available");
                    requestLocationPermission();
                    setLocationFromPrefs();
                } else {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.location_offer_to_enable_gps_title).setCancelable(false)
                            .setMessage(R.string.location_offer_to_enable)
                            .setPositiveButton(android.R.string.ok, (dialog12, which) -> {
                                Log.d(TAG, "Sending to editor location prefs page");
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                activity.startActivity(intent);
                            })
                            .setNegativeButton(android.R.string.cancel, (dialog1, which) -> {
                                Snackbar.make(rootView, R.string.msg_manual_location_on, Snackbar.LENGTH_SHORT).show();
                                preferences.edit().putBoolean(ApplicationConstants.NO_AUTO_LOCATE_PREF, true).apply();
                                setLocationFromPrefs();
                            }).show();
                }
                return true;
            }

            Log.d(TAG, "Got location provider " + locationProvider);
            locationManager.requestLocationUpdates(locationProvider, LOCATION_UPDATE_TIME_MS,
                    MINIMUM_DISTANCE_UPDATES_METRES, this, Looper.getMainLooper());
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) onLocationOk(location);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "Caught " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    public void restartLocation() {
        try {
            String locationProvider = locationManager.getBestProvider(getLocationCriteria(), true);
            if (locationProvider != null) {
                locationManager.requestLocationUpdates(locationProvider, LOCATION_UPDATE_TIME_MS, MINIMUM_DISTANCE_UPDATES_METRES, this);
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location != null) onLocationOk(location);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Caught " + e.getLocalizedMessage(), e);
        }
    }

    private Criteria getLocationCriteria() {
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(preferences.getBoolean(ApplicationConstants.FORCE_GPS_PREF, false) ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_COARSE);
        locationCriteria.setAltitudeRequired(true);
        locationCriteria.setBearingRequired(false);
        locationCriteria.setCostAllowed(true);
        locationCriteria.setSpeedRequired(false);
        locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        return locationCriteria;
    }

    protected void requestLocationPermission() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.warning).setCancelable(false)
                .setMessage(R.string.location_no_auto)
                .setNegativeButton(android.R.string.cancel, (dialog12, which) -> {
                    Snackbar.make(rootView, R.string.msg_manual_location_on, Snackbar.LENGTH_SHORT).show();
                    preferences.edit().putBoolean(ApplicationConstants.NO_AUTO_LOCATE_PREF, true).apply();
                    setLocationFromPrefs();
                })
                .setPositiveButton(R.string.take_me_there, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                    activity.startActivity(intent);
                }).show();
    }

    private void setLocationFromPrefs() {
        Log.d(TAG, "Setting location from preferences");
        double latitude = 0.0, longitude = 0.0;
        try {
            latitude = Double.parseDouble(preferences.getString(ApplicationConstants.LATITUDE_PREF, "0"));
            longitude = Double.parseDouble(preferences.getString(ApplicationConstants.LONGITUDE_PREF, "0"));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing latitude or longitude preference");
            Snackbar.make(rootView, R.string.malformed_loc_error, Snackbar.LENGTH_SHORT).show();
        }
        Location location = new Location(activity.getString(R.string.preferences));
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(0.0f);
        Log.d(TAG, "Latitude " + longitude);
        Log.d(TAG, "Longitude " + latitude);
        onLocationOk(location);
    }

    public void stop() {
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        if (location == null) {
            Log.e(TAG, "Didn't get location even though onLocationChanged called");
            setLocationFromPrefs();
            return;
        }
        onLocationOk(location);
        // Only need get the location once.
        locationManager.removeUpdates(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    protected abstract void onLocationOk(Location location);

    public void showLocationToUser(Location location, String provider) {
        Log.d(TAG, "Reverse geocoding location");
        List<Address> addresses = new ArrayList<>();
        String place;
        try {
            addresses = new Geocoder(activity).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.e(TAG, "Unable to reverse geocode location " + location);
        }
        if (addresses == null || addresses.isEmpty()) {
            Log.d(TAG, "No addresses returned");
            place = String.format(activity.getString(R.string.location_long_lat), location.getLongitude(), location.getLatitude());
        } else {
            place = getPlaceSummary(location, addresses.get(0));
        }
        Log.d(TAG, "Location set to " + place);
        Snackbar.make(rootView, String.format(activity.getString(R.string.location_set_auto), provider, place),
                Snackbar.LENGTH_SHORT).show();
    }

    private String getPlaceSummary(Location location, Address address) {
        String longLat = String.format(activity.getString(R.string.location_long_lat),
                location.getLongitude(), location.getLatitude());
        if (address == null) {
            return longLat;
        }
        String place = address.getLocality();
        if (place == null) {
            place = address.getSubAdminArea();
        }
        if (place == null) {
            place = address.getAdminArea();
        }
        if (place == null) {
            place = longLat;
        }
        return place;
    }
}