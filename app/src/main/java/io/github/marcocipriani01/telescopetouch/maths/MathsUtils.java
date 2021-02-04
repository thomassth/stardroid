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

package io.github.marcocipriani01.telescopetouch.maths;

import android.location.Location;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.TimeUtils;

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
    public static final double DEGREES_TO_RADIANS = (float) Math.PI / 180.0f;
    // Convert Radians to Degrees
    public static final double RADIANS_TO_DEGREES = 180.0f / (float) Math.PI;
    public static final int ONE = 0x00010000;

    private MathsUtils() {
    }

    /**
     * Return the integer part of a number
     */
    public static double absFloor(double x) {
        if (x >= 0.0)
            return Math.floor(x);
        else
            return Math.ceil(x);
    }

    /**
     * Returns the modulo the given value by 2\pi. Returns an angle in the range 0
     * to 2\pi radians.
     */
    public static double mod2pi(double x) {
        double factor = x / (2.0f * Math.PI);
        double result = 2.0 * Math.PI * (factor - absFloor(factor));
        if (result < 0.0) {
            result = 2.0 * Math.PI + result;
        }
        return result;
    }

    /**
     * Converts a float to a 16.16 fixed point number.
     */
    public static int floatToFixedPoint(float f) {
        return (int) (65536F * f);
    }
}