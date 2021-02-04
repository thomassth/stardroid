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
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.maths.Formatters;
import io.github.marcocipriani01.telescopetouch.sensors.LocationHelper;

/**
 * Sets the AstronomerModel's (and thus the user's) position using one of the
 * network, GPS or user-set preferences.
 *
 * @author John Taylor
 */
public class LocationController extends AbstractController {

    private static final String TAG = TelescopeTouchApp.getTag(LocationController.class);
    private static final float MIN_DIST_TO_SHOW_TOAST_DEGREES = 0.01f;
    private final LocationHelper locationHelper;

    @Inject
    public LocationController(Context context, LocationManager locationManager) {
        locationHelper = new LocationHelper(context, locationManager) {
            @Override
            protected void onLocationOk(Location location) {
                setLocationInModel(location, location.getProvider());
            }
        };
    }

    @Override
    public void start() {
        locationHelper.start();
    }

    protected void setLocationInModel(Location location, String provider) {
        if (Formatters.locationDistance(location, model.getLocation()) > MIN_DIST_TO_SHOW_TOAST_DEGREES) {
            Log.d(TAG, "Informing user of change of location");
            locationHelper.showLocationToUser(location, provider);
        } else {
            Log.d(TAG, "Location not changed sufficiently to tell the user");
        }
        model.setLocation(location);
    }

    public Location getCurrentLocation() {
        return model.getLocation();
    }

    @Override
    public void stop() {
        locationHelper.stop();
    }
}