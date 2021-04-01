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

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utilities for working with Dates and times.
 *
 * @author Kevin Serafini
 * @author Brent Bryan
 * @author marcocipriani01
 */
public class TimeUtils {

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

    private TimeUtils() {
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
                (calendar.get(Calendar.HOUR_OF_DAY) / 24.0) +
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
    public static double meanSiderealTime(Calendar calendar, double longitude) {
        double jd = julianDayGreenwich(calendar),
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
}