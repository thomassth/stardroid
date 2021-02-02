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

package io.github.marcocipriani01.telescopetouch.units;

import io.github.marcocipriani01.telescopetouch.util.MathsUtils;
import io.github.marcocipriani01.telescopetouch.util.Vector3;

/**
 * This class corresponds to an object's location in Euclidean space
 * when it is projected onto a unit sphere (with the Earth at the
 * center).
 *
 * @author Brent Bryan
 */
public class GeocentricCoordinates extends Vector3 {

    public GeocentricCoordinates(float x, float y, float z) {
        super(x, y, z);
    }

    /**
     * Convert ra and dec to x,y,z where the point is place on the unit sphere.
     */
    public static GeocentricCoordinates getInstance(RaDec raDec) {
        return getInstance(raDec.ra, raDec.dec);
    }

    public static GeocentricCoordinates getInstance(float ra, float dec) {
        GeocentricCoordinates coords = new GeocentricCoordinates(0.0f, 0.0f, 0.0f);
        coords.updateFromRaDec(ra, dec);
        return coords;
    }

    public static GeocentricCoordinates getInstance(double ra, double dec) {
        GeocentricCoordinates coords = new GeocentricCoordinates(0.0f, 0.0f, 0.0f);
        coords.updateFromRaDec((float) ra, (float) dec);
        return coords;
    }

    /**
     * Convert ra and dec to x,y,z where the point is place on the unit sphere.
     */
    public static GeocentricCoordinates getInstanceFromFloatArray(float[] xyz) {
        return new GeocentricCoordinates(xyz[0], xyz[1], xyz[2]);
    }

    public static GeocentricCoordinates getInstanceFromVector3(Vector3 v) {
        return new GeocentricCoordinates(v.x, v.y, v.z);
    }

    /**
     * Recomputes the x, y, and z variables in this class based on the specified
     * {@link RaDec}.
     */
    public void updateFromRaDec(RaDec raDec) {
        updateFromRaDec(raDec.ra, raDec.dec);
    }

    /**
     * Updates these coordinates with the given ra and dec in degrees.
     */
    public void updateFromRaDec(float ra, float dec) {
        float raRadians = ra * MathsUtils.DEGREES_TO_RADIANS;
        float decRadians = dec * MathsUtils.DEGREES_TO_RADIANS;
        this.x = (float) Math.cos(raRadians) * (float) Math.cos(decRadians);
        this.y = (float) Math.sin(raRadians) * (float) Math.cos(decRadians);
        this.z = (float) Math.sin(decRadians);
    }

    /**
     * Updates these coordinates with the given ra and dec in degrees.
     */
    public void updateFromRaDec(double ra, double dec) {
        updateFromRaDec((float) ra, (float) dec);
    }

    /**
     * Returns the RA in degrees
     */
    public float getRa() {
        // Assumes unit sphere.
        return MathsUtils.RADIANS_TO_DEGREES * (float) Math.atan2(y, x);
    }

    /**
     * Returns the declination in degrees
     */
    public float getDec() {
        // Assumes unit sphere.
        return MathsUtils.RADIANS_TO_DEGREES * (float) Math.asin(z);
    }

    @Override
    public float[] toFloatArray() {
        return new float[]{x, y, z};
    }

    /**
     * Assumes it's an array of length 3.
     */
    private void updateFromFloatArray(float[] xyz) {
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }

    private void updateFromVector3(Vector3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    @Override
    public GeocentricCoordinates copy() {
        return new GeocentricCoordinates(x, y, z);
    }
}