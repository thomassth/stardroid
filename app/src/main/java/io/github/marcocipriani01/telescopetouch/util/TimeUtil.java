package io.github.marcocipriani01.telescopetouch.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utilities for working with Dates and times.
 *
 * @author Kevin Serafini
 * @author Brent Bryan
 */
public class TimeUtil {

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

    private TimeUtil() {
    }

    /**
     * Calculate the number of Julian Centuries from the epoch 2000.0
     * (equivalent to Julian Day 2451545.0).
     */
    public static double julianCenturies(Calendar date) {
        double jd = calculateJulianDay(date);
        double delta = jd - 2451545.0;
        return delta / 36525.0;
    }

    /**
     * Calculate the Julian Day for a given date using the following formula:
     * JD = 367 * Y - INT(7 * (Y + INT((M + 9)/12))/4) + INT(275 * M / 9)
     * + D + 1721013.5 + UT/24
     * <p>
     * Note that this is only valid for the year range 1900 - 2099.
     */
    public static double calculateJulianDay(Calendar date) {
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        double hour = date.get(Calendar.HOUR_OF_DAY)
                + date.get(Calendar.MINUTE) / 60.0f
                + date.get(Calendar.SECOND) / 3600.0f;
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        return 367.0 * year - Math.floor(7.0 * (year
                + Math.floor((month + 9.0) / 12.0)) / 4.0)
                + Math.floor(275.0 * month / 9.0) + day
                + 1721013.5 + hour / 24.0;
    }

    /**
     * Calculate local mean sidereal time in degrees. Note that longitude is
     * negative for western longitude values.
     */
    public static float meanSiderealTime(Calendar date, float longitude) {
        // First, calculate number of Julian days since J2000.0.
        double jd = calculateJulianDay(date);
        double delta = jd - 2451545.0f;

        // Calculate the global and local sidereal times
        double gst = 280.461f + 360.98564737f * delta;
        double lst = normalizeAngle(gst + longitude);

        return (float) lst;
    }

    /**
     * Normalize the angle to the range 0 <= value < 360.
     */
    public static double normalizeAngle(double angle) {
        double remainder = angle % 360;
        if (remainder < 0) remainder += 360;
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