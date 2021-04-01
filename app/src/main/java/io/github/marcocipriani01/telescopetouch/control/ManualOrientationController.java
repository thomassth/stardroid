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

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Matrix3x3;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

/**
 * Allows user-input elements such as touch screens and trackballs to move the
 * map.
 *
 * @author John Taylor
 */
public class ManualOrientationController extends AbstractController {

    @Override
    public void start() {
        // Nothing to do
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    /**
     * Moves the astronomer's pointing right or left.
     *
     * @param radians the angular change in the pointing in radians (only
     *                accurate in the limit as radians tends to 0.)
     */
    public void changeRightLeft(float radians) {
        // TODO(johntaylor): Some of the Math in here perhaps belongs in
        // AstronomerModel.
        if (!enabled) {
            return;
        }
        Pointing pointing = model.getPointing();
        GeocentricCoordinates pointingXyz = pointing.getLineOfSight();
        GeocentricCoordinates topXyz = pointing.getPerpendicular();
        Vector3 horizontalXyz = Vector3.vectorProduct(pointingXyz, topXyz);
        Vector3 deltaXyz = Vector3.scale(horizontalXyz, radians);

        Vector3 newPointingXyz = Vector3.sum(pointingXyz, deltaXyz);
        newPointingXyz.normalize();

        model.setPointing(newPointingXyz, topXyz);
    }

    /**
     * Moves the astronomer's pointing up or down.
     *
     * @param radians the angular change in the pointing in radians (only
     *                accurate in the limit as radians tends to 0.)
     */
    public void changeUpDown(float radians) {
        if (!enabled) {
            return;
        }
        // Log.d(TAG, "Scrolling up down");
        Pointing pointing = model.getPointing();
        GeocentricCoordinates pointingXyz = pointing.getLineOfSight();
        // Log.d(TAG, "Current view direction " + viewDir);
        GeocentricCoordinates topXyz = pointing.getPerpendicular();

        Vector3 deltaXyz = Vector3.scale(topXyz, -radians);
        Vector3 newPointingXyz = Vector3.sum(pointingXyz, deltaXyz);
        newPointingXyz.normalize();

        Vector3 deltaUpXyz = Vector3.scale(pointingXyz, radians);
        Vector3 newUpXyz = Vector3.sum(topXyz, deltaUpXyz);
        newUpXyz.normalize();

        model.setPointing(newPointingXyz, newUpXyz);
    }

    /**
     * Rotates the astronomer's view.
     */
    public void rotate(float degrees) {
        if (!enabled) {
            return;
        }
        Pointing pointing = model.getPointing();
        GeocentricCoordinates pointingXyz = pointing.getLineOfSight();
        Matrix3x3 rotation = Matrix3x3.calculateRotationMatrix(degrees, pointingXyz);

        GeocentricCoordinates topXyz = pointing.getPerpendicular();
        Vector3 newUpXyz = Matrix3x3.matrixVectorMultiply(rotation, topXyz);
        newUpXyz.normalize();

        model.setPointing(pointingXyz, newUpXyz);
    }
}