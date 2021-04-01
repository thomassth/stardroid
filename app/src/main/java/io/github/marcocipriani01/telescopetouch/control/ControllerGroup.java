/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.control;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.SkyMapActivity;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Formatters;
import io.github.marcocipriani01.telescopetouch.sensors.LocationHelper;

/**
 * Manages all the different controllers that affect the model of the observer.
 * Is both a factory and acts as a facade to the underlying controllers.
 *
 * @author John Taylor
 */
public class ControllerGroup implements Controller {

    private final static String TAG = TelescopeTouchApp.getTag(ControllerGroup.class);
    private static final float MIN_DISTANCE_MESSAGE_DEG = 0.01f;
    private final ArrayList<Controller> controllers = new ArrayList<>();
    private final ZoomController zoomController;
    private final ManualOrientationController manualDirectionController;
    private final SensorOrientationController sensorOrientationController;
    private final TimeTravelClock timeTravelClock = new TimeTravelClock();
    private final TransitioningCompositeClock transitioningClock = new TransitioningCompositeClock(timeTravelClock, new RealClock());
    private final TeleportingController teleportingController;
    private final LocationHelper locationHelper;
    private boolean usingAutoMode = true;
    private AstronomerModel model;
    private Location location;

    @Inject
    ControllerGroup(SkyMapActivity activity, LocationManager locationManager, SensorOrientationController sensorOrientationController) {
        locationHelper = new LocationHelper(activity, locationManager) {
            @Override
            protected void onLocationOk(Location location) {
                ControllerGroup.this.location = location;
                if (model != null) setLocationInModel();
            }

            @Override
            protected void requestLocationPermission() {
                activity.requestLocationPermission();
            }
        };
        this.sensorOrientationController = sensorOrientationController;
        controllers.add(sensorOrientationController);
        manualDirectionController = new ManualOrientationController();
        controllers.add(manualDirectionController);
        zoomController = new ZoomController();
        controllers.add(zoomController);
        teleportingController = new TeleportingController();
        controllers.add(teleportingController);
        setAutoMode(true);
    }

    public void onLocationPermissionAcquired() {
        locationHelper.restartLocation();
    }

    private void setLocationInModel() {
        if (Formatters.locationDistance(location, model.getLocation()) > MIN_DISTANCE_MESSAGE_DEG) {
            Log.d(TAG, "Informing user of change of location");
            locationHelper.showLocationToUser(location, location.getProvider());
        } else {
            Log.d(TAG, "Location not changed sufficiently to tell the user");
        }
        model.setLocation(location);
    }

    @Override
    public void setEnabled(boolean enabled) {
        Log.i(TAG, "Enabling all controllers");
        for (Controller controller : controllers) {
            controller.setEnabled(enabled);
        }
    }

    @Override
    public void setModel(AstronomerModel model) {
        Log.i(TAG, "Setting model");
        this.model = model;
        if (location != null)
            model.setLocation(location);
        for (Controller controller : controllers) {
            controller.setModel(model);
        }
        model.setAutoUpdatePointing(usingAutoMode);
        model.setClock(transitioningClock);
    }

    /**
     * Switches to time-travel model and start with the supplied time.
     * See {@link #useRealTime()}.
     */
    public void goTimeTravel(Date d) {
        transitioningClock.goTimeTravel(d);
    }

    /**
     * Gets the id of the string used to display the current speed of time travel.
     */
    public int getCurrentSpeedTag() {
        return timeTravelClock.getCurrentSpeedTag();
    }

    /**
     * Sets the model back to using real time.
     * See {@link #goTimeTravel(Date)}.
     */
    public void useRealTime() {
        transitioningClock.returnToRealTime();
    }

    /**
     * Increases the rate of time travel into the future (or decreases the rate of
     * time travel into the past) if in time travel mode.
     */
    public void accelerateTimeTravel() {
        timeTravelClock.accelerateTimeTravel();
    }

    /**
     * Decreases the rate of time travel into the future (or increases the rate of
     * time travel into the past) if in time travel mode.
     */
    public void decelerateTimeTravel() {
        timeTravelClock.decelerateTimeTravel();
    }

    /**
     * Pauses time, if in time travel mode.
     */
    public void pauseTime() {
        timeTravelClock.pauseTime();
    }

    /**
     * Are we in auto mode (aka sensor mode) or manual?
     */
    public boolean isAutoMode() {
        return usingAutoMode;
    }

    /**
     * Sets auto mode (true) or manual mode (false).
     */
    public void setAutoMode(boolean enabled) {
        manualDirectionController.setEnabled(!enabled);
        sensorOrientationController.setEnabled(enabled);
        if (model != null)
            model.setAutoUpdatePointing(enabled);
        usingAutoMode = enabled;
    }

    @Override
    public void start() {
        Log.i(TAG, "Starting controllers");
        for (Controller controller : controllers) {
            controller.start();
        }
        locationHelper.start();
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stopping controllers");
        for (Controller controller : controllers) {
            controller.stop();
        }
        locationHelper.stop();
    }

    /**
     * Moves the pointing right and left.
     *
     * @param radians the angular change in the pointing in radians (only
     *                accurate in the limit as radians tends to 0.)
     */
    public void changeRightLeft(float radians) {
        manualDirectionController.changeRightLeft(radians);
    }

    /**
     * Moves the pointing up and down.
     *
     * @param radians the angular change in the pointing in radians (only
     *                accurate in the limit as radians tends to 0.)
     */
    public void changeUpDown(float radians) {
        manualDirectionController.changeUpDown(radians);
    }

    /**
     * Rotates the view about the current center point.
     */
    public void rotate(float degrees) {
        manualDirectionController.rotate(degrees);
    }

    /**
     * Sends the astronomer's pointing to the new target.
     *
     * @param target the destination
     */
    public void teleport(GeocentricCoordinates target) {
        teleportingController.teleport(target);
    }

    public void zoomBy(float ratio) {
        zoomController.zoomBy(ratio);
    }
}
