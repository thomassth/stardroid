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

package io.github.marcocipriani01.telescopetouch.maths;

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