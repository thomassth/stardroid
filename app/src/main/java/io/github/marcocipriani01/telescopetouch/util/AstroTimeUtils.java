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

import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import io.github.marcocipriani01.telescopetouch.catalog.CatalogCoordinates;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Utilities for working with Dates and times.
 *
 * @author Kevin Serafini
 * @author Brent Bryan
 */
public class AstroTimeUtils {

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long MILLISECONDS_PER_MINUTE = 60000L;
    public static final long MILLISECONDS_PER_HOUR = 3600000L;
    public static final long MILLISECONDS_PER_DAY = 86400000L;
    public static final long SECONDS_PER_SECOND = 1L;
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SECONDS_PER_10MINUTE = 600L;
    public static final long SECONDS_PER_HOUR = 3600L;
    public static final long SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
    public static final long SECONDS_PER_WEEK = 7 * SECONDS_PER_DAY;

    private AstroTimeUtils() {
    }

    public static double julianDayGreenwich(Calendar calendar) {
        Calendar greenwich = ((Calendar) calendar.clone());
        greenwich.setTimeZone(TimeZone.getTimeZone("GMT"));
        return julianDay(greenwich);
    }

    public static double julianDayToCentury(double jd) {
        return (jd - 2451545.0) / 36525.0;
    }

    /**
     * Equation taken from the book "Astronomical Algorithms" by Jean Meeus
     * (published by Willmann-Bell, Inc., Richmond, VA). Chapter 7, Julian Day.
     *
     * @see <a href="https://www.willbell.com/math/mc1.HTM">"Astronomical Algorithms" by Jean Meeus</a>
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static double julianDay(Calendar calendar) {
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH) + 1;
        double d = calendar.get(Calendar.DATE) +
                (calendar.get(Calendar.HOUR) / 24.0) +
                (calendar.get(Calendar.MINUTE) / 1440.0) +
                (calendar.get(Calendar.SECOND) / 86400.0);
        if ((m == 1) || (m == 2)) {
            y--;
            m += 12;
        }
        return ((int) (365.25 * (y + 4716.0))) +
                ((int) (30.6001 * (m + 1.0))) +
                d + (2 - ((3 * y) / 400)) - 1524.5;
    }

    /**
     * Calculate local mean sidereal time in degrees. Note that longitude is
     * negative for western longitude values.
     */
    public static double meanSiderealTime(Calendar date, float longitude) {
        double jd = julianDayGreenwich(date),
                t = julianDayToCentury(jd),
                t2 = t * t,
                t3 = t2 * t,
                gst = 280.46061837 + (360.98564736629 * (jd - 2451545.0)) + (0.000387933 * t2) - (t3 / 38710000);
        return normalizeAngle(gst + longitude);
    }

    /**
     * Normalize the angle to the range 0 <= value < 360.
     */
    public static double normalizeAngle(double angle) {
        double remainder = angle % 360.0;
        if (remainder < 0.0) remainder += 360.0;
        return remainder;
    }

    /**
     * Normalize the time to the range 0 <= value < 24.
     */
    public static double normalizeHours(double time) {
        double remainder = time % 24;
        if (remainder < 0) remainder += 24;
        return remainder;
    }

    /**
     * Equation taken from the book "Astronomical Algorithms" by Jean Meeus
     * (published by Willmann-Bell, Inc., Richmond, VA). Chapter 21, Precession.
     *
     * @see <a href="https://www.willbell.com/math/mc1.HTM">"Astronomical Algorithms" by Jean Meeus</a>
     */
    public static CatalogCoordinates precess(Calendar calendar, CatalogCoordinates in) {
        double dec0 = toRadians(in.getDec()),
                t = julianDayToCentury(julianDay(calendar)),
                t2 = t * t, t3 = t2 * t,
                zeta = (0.6406161389 * t) + (0.00008385555556 * t2) + (0.000004999444444 * t3),
                theta = toRadians((0.5567530278 * t) - (0.0001185138889 * t2) - (0.00001162027778 * t3)),
                sinTheta = sin(theta),
                cosTheta = cos(theta),
                tmp = cos(dec0) * cos(toRadians(in.getRa() + zeta));
        CatalogCoordinates out = new CatalogCoordinates(toDegrees(
                atan2(cos(dec0) * sin(toRadians(in.getRa() + zeta)), (cosTheta * tmp) - (sinTheta * sin(dec0)))) +
                (0.6406161389 * t) + (0.0003040777778 * t2) + (0.000005056388889 * t3),
                toDegrees(asin((sinTheta * tmp) + (cosTheta * sin(dec0)))));
        Log.d("AstroUtils", "Result: " + out.toString());
        return out;
    }
}