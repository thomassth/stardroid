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

import android.util.Log;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

/**
 * Flies the user to the search target in manual mode.
 *
 * @author John Taylor
 */
public class TeleportingController extends AbstractController {

    private static final String TAG = TelescopeTouchApp.getTag(TeleportingController.class);

    /**
     * Teleport the astronomer instantaneously from his current pointing to a new
     * one.
     *
     * @param targetXyz The destination pointing.
     */
    public void teleport(final GeocentricCoordinates targetXyz) {
        Log.d(TAG, "Teleporting to target " + targetXyz);
        Pointing pointing = model.getPointing();
        final GeocentricCoordinates hereXyz = pointing.getLineOfSight();
        if (targetXyz.equals(hereXyz)) {
            return;
        }

        // Here we calculate the new direction of 'up' along the screen in
        // celestial coordinates.  This is not uniquely defined - it just needs
        // to be perpendicular to the target (which is effectively the normal into
        // the screen in celestial coordinates.)
        Vector3 hereTopXyz = pointing.getPerpendicular();
        hereTopXyz.normalize();
        final Vector3 normal = Vector3.vectorProduct(hereXyz, hereTopXyz);
        Vector3 newUpXyz = Vector3.vectorProduct(normal, targetXyz);

        model.setPointing(targetXyz, newUpXyz);
    }

    @Override
    public void start() {
        // Nothing to do.
    }

    @Override
    public void stop() {
        // Nothing to do.
        // We could consider aborting the teleport, but it's OK for now.
    }
}