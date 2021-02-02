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

package io.github.marcocipriani01.telescopetouch.util;

import android.location.Location;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.units.RaDec;

/**
 * Utilities for working with angles, distances, matrices, and time.
 *
 * @author Kevin Serafini
 * @author Brent Bryan
 * @author Dominic Widdows
 * @author John Taylor
 */
public final class MathsUtils {

    // Convert Degrees to Radians
    public static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0f;
    // Convert Radians to Degrees
    public static final float RADIANS_TO_DEGREES = 180.0f / (float) Math.PI;
    public static final int ONE = 0x00010000;

    private MathsUtils() {
    }

    /**
     * Return the integer part of a number
     */
    public static float abs_floor(float x) {
        float result;
        if (x >= 0.0)
            result = (float) Math.floor(x);
        else
            result = (float) Math.ceil(x);
        return result;
    }

    /**
     * Returns the modulo the given value by 2\pi. Returns an angle in the range 0
     * to 2\pi radians.
     */
    public static float mod2pi(float x) {
        float factor = x / (2f * (float) Math.PI);
        float result = 2f * (float) Math.PI * (factor - abs_floor(factor));
        if (result < 0.0) {
            result = 2f * (float) Math.PI + result;
        }
        return result;
    }

    /**
     * Compute celestial coordinates of zenith from utc, lat long.
     */
    public static RaDec calculateRADecOfZenith(Calendar utc, Location location) {
        // compute overhead RA in degrees
        return new RaDec((float) TimeUtils.meanSiderealTime(utc, location.getLongitude()), (float) location.getLatitude());
    }

    /**
     * Converts a float to a 16.16 fixed point number.
     */
    public static int floatToFixedPoint(float f) {
        return (int) (65536F * f);
    }
}