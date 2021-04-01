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

package io.github.marcocipriani01.telescopetouch.astronomy;

import io.github.marcocipriani01.telescopetouch.maths.MathsUtils;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

/**
 * This class corresponds to an object's location in Euclidean space
 * when it is projected onto a unit sphere (with the Earth at the
 * center).
 *
 * @author Brent Bryan
 */
public class GeocentricCoordinates extends Vector3 {

    public GeocentricCoordinates() {
        super(0, 0, 0);
    }

    public GeocentricCoordinates(double x, double y, double z) {
        super(x, y, z);
    }

    /**
     * Convert ra and dec to x,y,z where the point is place on the unit sphere.
     */
    public static GeocentricCoordinates getInstance(EquatorialCoordinates raDec) {
        return getInstance(raDec.ra, raDec.dec);
    }

    public static GeocentricCoordinates getInstance(float ra, float dec) {
        GeocentricCoordinates coords = new GeocentricCoordinates();
        coords.updateFromRaDec(ra, dec);
        return coords;
    }

    public static GeocentricCoordinates getInstance(double ra, double dec) {
        GeocentricCoordinates coords = new GeocentricCoordinates();
        coords.updateFromRaDec((float) ra, (float) dec);
        return coords;
    }

    public static GeocentricCoordinates getInstance(Vector3 v) {
        return new GeocentricCoordinates(v.x, v.y, v.z);
    }

    /**
     * Recomputes the x, y, and z variables in this class based on the specified
     * {@link EquatorialCoordinates}.
     */
    public void updateFromRaDec(EquatorialCoordinates raDec) {
        updateFromRaDec(raDec.ra, raDec.dec);
    }

    /**
     * Updates these coordinates with the given ra and dec in degrees.
     */
    public void updateFromRaDec(double ra, double dec) {
        double raRadians = ra * MathsUtils.DEGREES_TO_RADIANS;
        double decRadians = dec * MathsUtils.DEGREES_TO_RADIANS;
        this.x = Math.cos(raRadians) * Math.cos(decRadians);
        this.y = Math.sin(raRadians) * Math.cos(decRadians);
        this.z = Math.sin(decRadians);
    }

    /**
     * Returns the RA in degrees
     */
    public double getRa() {
        // Assumes unit sphere.
        return MathsUtils.RADIANS_TO_DEGREES * Math.atan2(y, x);
    }

    /**
     * Returns the declination in degrees
     */
    public double getDec() {
        // Assumes unit sphere.
        return MathsUtils.RADIANS_TO_DEGREES * Math.asin(z);
    }

    @Override
    public GeocentricCoordinates copy() {
        return new GeocentricCoordinates(x, y, z);
    }
}