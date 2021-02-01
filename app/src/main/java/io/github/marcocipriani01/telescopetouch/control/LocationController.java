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

package io.github.marcocipriani01.telescopetouch.control;

import android.content.Context;
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
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.units.LatLong;

/**
 * Sets the AstronomerModel's (and thus the user's) position using one of the
 * network, GPS or user-set preferences.
 *
 * @author John Taylor
 */
public class LocationController extends AbstractController implements LocationListener {

    // Must match the key in the preferences file.
    public static final String NO_AUTO_LOCATE = "no_auto_locate";
    private static final String TAG = TelescopeTouchApp.getTag(LocationController.class);
    // Must match the key in the preferences file.
    private static final String FORCE_GPS = "force_gps";
    private static final int MINIMUM_DISTANCE_BEFORE_UPDATE_METRES = 2000;
    private static final int LOCATION_UPDATE_TIME_MILLISECONDS = 600000;
    private static final float MIN_DIST_TO_SHOW_TOAST_DEGREES = 0.01f;
    private final Context context;
    private final LocationManager locationManager;

    @Inject
    public LocationController(Context context, LocationManager locationManager) {
        this.context = context;
        if (locationManager != null) {
            Log.d(TAG, "Got location Manager");
        } else {
            Log.d(TAG, "Didn't get location manager");
        }
        this.locationManager = locationManager;
    }

    @Override
    public void start() {
        Log.d(TAG, "LocationController start");
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(NO_AUTO_LOCATE, false)) {
            Log.d(TAG, "User has elected to set location manually.");
            setLocationFromPrefs();
            Log.d(TAG, "LocationController -start");
            return;
        }

        try {
            if (locationManager == null) {
                Log.e(TAG, "Location manager was null - using preferences");
                setLocationFromPrefs();
                return;
            }

            Criteria locationCriteria = new Criteria();
            locationCriteria.setAccuracy(preferences.getBoolean(FORCE_GPS, false) ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_COARSE);
            locationCriteria.setAltitudeRequired(false);
            locationCriteria.setBearingRequired(false);
            locationCriteria.setCostAllowed(true);
            locationCriteria.setSpeedRequired(false);
            locationCriteria.setPowerRequirement(Criteria.POWER_LOW);

            String locationProvider = locationManager.getBestProvider(locationCriteria, true);
            if (locationProvider == null) {
                Log.w(TAG, "No location provider is enabled");
                String possibleLocationProvider = locationManager.getBestProvider(locationCriteria, false);
                if (possibleLocationProvider == null) {
                    Log.i(TAG, "No location provider is even available");
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.warning).setCancelable(false)
                            .setMessage(R.string.location_no_auto)
                            .setNegativeButton(android.R.string.cancel, (dialog12, which) -> {
                                Toast.makeText(context, "Switching manual location on...", Toast.LENGTH_SHORT).show();
                                preferences.edit().putBoolean(NO_AUTO_LOCATE, true).apply();
                                setLocationFromPrefs();
                            })
                            .setPositiveButton(R.string.take_me_there, (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                                context.startActivity(intent);
                            }).show();
                    setLocationFromPrefs();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.location_offer_to_enable_gps_title).setCancelable(false)
                            .setMessage(R.string.location_offer_to_enable)
                            .setPositiveButton(android.R.string.ok, (dialog12, which) -> {
                                Log.d(TAG, "Sending to editor location prefs page");
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            })
                            .setNegativeButton(android.R.string.cancel, (dialog1, which) -> {
                                Toast.makeText(context, "Switching manual location on...", Toast.LENGTH_SHORT).show();
                                preferences.edit().putBoolean(NO_AUTO_LOCATE, true).apply();
                                setLocationFromPrefs();
                            }).show();
                }
                return;
            } else {
                Log.d(TAG, "Got location provider " + locationProvider);
            }

            locationManager.requestLocationUpdates(locationProvider, LOCATION_UPDATE_TIME_MILLISECONDS,
                    MINIMUM_DISTANCE_BEFORE_UPDATE_METRES, this);
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                LatLong myLocation = new LatLong(location.getLatitude(), location.getLongitude());
                setLocationInModel(myLocation, location.getProvider());
            }
        } catch (SecurityException securityException) {
            Log.d(TAG, "Caught " + securityException);
            Log.d(TAG, "Most likely user has not enabled this permission");
        }
    }

    protected void setLocationInModel(LatLong location, String provider) {
        LatLong oldLocation = model.getLocation();
        if (location.distanceFrom(oldLocation) > MIN_DIST_TO_SHOW_TOAST_DEGREES) {
            Log.d(TAG, "Informing user of change of location");
            showLocationToUser(location, provider);
        } else {
            Log.d(TAG, "Location not changed sufficiently to tell the user");
        }
        model.setLocation(location);
    }

    public LatLong getCurrentLocation() {
        return model.getLocation();
    }

    private void setLocationFromPrefs() {
        Log.d(TAG, "Setting location from preferences");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String longitudeString = preferences.getString("longitude", "0"),
                latitudeString = preferences.getString("latitude", "0");
        float longitude = 0, latitude = 0;
        try {
            longitude = Float.parseFloat(longitudeString);
            latitude = Float.parseFloat(latitudeString);
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Error parsing latitude or longitude preference");
            Toast.makeText(context, R.string.malformed_loc_error, Toast.LENGTH_SHORT).show();
        }
        Location location = new Location(context.getString(R.string.preferences));
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        Log.d(TAG, "Latitude " + longitude);
        Log.d(TAG, "Longitude " + latitude);
        LatLong myPosition = new LatLong(latitude, longitude);
        setLocationInModel(myPosition, context.getString(R.string.preferences));
    }

    @Override
    public void stop() {
        Log.d(TAG, "LocationController stop");

        if (locationManager == null) {
            return;
        }
        locationManager.removeUpdates(this);

        Log.d(TAG, "LocationController -stop");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "LocationController onLocationChanged");

        if (location == null) {
            Log.e(TAG, "Didn't get location even though onLocationChanged called");
            setLocationFromPrefs();
            return;
        }

        LatLong newLocation = new LatLong(location.getLatitude(), location.getLongitude());

        Log.d(TAG, "Latitude " + newLocation.getLatitude());
        Log.d(TAG, "Longitude " + newLocation.getLongitude());
        setLocationInModel(newLocation, location.getProvider());

        // Only need get the location once.
        locationManager.removeUpdates(this);

        Log.d(TAG, "LocationController -onLocationChanged");
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

    private void showLocationToUser(LatLong location, String provider) {
        Log.d(TAG, "Reverse geocoding location");
        List<Address> addresses = new ArrayList<>();
        String place;
        try {
            addresses = new Geocoder(context).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.e(TAG, "Unable to reverse geocode location " + location);
        }
        if (addresses == null || addresses.isEmpty()) {
            Log.d(TAG, "No addresses returned");
            place = String.format(context.getString(R.string.location_long_lat), location.getLongitude(), location.getLatitude());
        } else {
            place = getSummaryOfPlace(location, addresses.get(0));
        }
        Log.d(TAG, "Location set to " + place);
        Toast.makeText(context, String.format(context.getString(R.string.location_set_auto), provider, place),
                Toast.LENGTH_SHORT).show();
    }

    private String getSummaryOfPlace(LatLong location, Address address) {
        String template = context.getString(R.string.location_long_lat);
        String longLat = String.format(template, location.getLongitude(), location.getLatitude());
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