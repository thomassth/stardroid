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

public class StarsPrecession {

    private StarsPrecession() {
    }

    public static EquatorialCoordinates precess(Calendar calendar, double ra, double dec) {
        double x = cos(dec) * cos(ra),
                y = cos(dec) * sin(ra),
                z = sin(dec),
                d1 = ((((double) calendar.get(Calendar.YEAR)) + (((double) calendar.get(Calendar.DAY_OF_YEAR)) / 365.0)) - 2000.0) / 100.0,
                d2 = (((((0.017998 * d1) + 0.30188) * d1) + 2306.2181) * d1) / 3600.0,
                d3 = (((((2.05E-4 * d1) + 0.7928) * d1) * d1) / 3600.0) + d2,
                d4 = ((2004.3109 - (((0.041833 * d1) + 0.42665) * d1)) * d1) / 3600.0,
                cosD2 = cos(d2), cosD3 = cos(d3), cosD4 = cos(d4),
                sinD2 = sin(d2), sinD3 = sin(d3), sinD4 = sin(d4),
                d6 = cosD3 * cosD4, d7 = sinD3 * cosD4;
        x = (((-sinD3 * sinD2) + (d6 * cosD2)) * x) + (((-sinD3 * cosD2) - (d6 * sinD2)) * y) + (((-cosD3) * sinD4) * z);
        y = (((cosD3 * sinD2) + (d7 * cosD2)) * x) + (((cosD3 * cosD2) - (d7 * sinD2)) * y) + ((-sinD3 * sinD4) * z);
        z = ((cosD2 * sinD4) * x) + (((-sinD4) * sinD2) * y) + (cosD4 * z);
        return new EquatorialCoordinates(atan2(y, x), atan(z / Math.sqrt((x * x) + (y * y))));
    }

    public static EquatorialCoordinates precess(Calendar calendar, EquatorialCoordinates in) {
        return precess(calendar, in.ra, in.dec);
    }

    public static GeocentricCoordinates precessGeocentric(Calendar calendar, double ra, double dec) {
        double x = cos(dec) * cos(ra),
                y = cos(dec) * sin(ra),
                z = sin(dec),
                d1 = ((((double) calendar.get(Calendar.YEAR)) + (((double) calendar.get(Calendar.DAY_OF_YEAR)) / 365.0)) - 2000.0) / 100.0,
                d2 = (((((0.017998 * d1) + 0.30188) * d1) + 2306.2181) * d1) / 3600.0,
                d3 = (((((2.05E-4 * d1) + 0.7928) * d1) * d1) / 3600.0) + d2,
                d4 = ((2004.3109 - (((0.041833 * d1) + 0.42665) * d1)) * d1) / 3600.0,
                cosD2 = cos(d2), cosD3 = cos(d3), cosD4 = cos(d4),
                sinD2 = sin(d2), sinD3 = sin(d3), sinD4 = sin(d4),
                d6 = cosD3 * cosD4, d7 = sinD3 * cosD4;
        return new GeocentricCoordinates((((-sinD3 * sinD2) + (d6 * cosD2)) * x) + (((-sinD3 * cosD2) - (d6 * sinD2)) * y) + (((-cosD3) * sinD4) * z),
                (((cosD3 * sinD2) + (d7 * cosD2)) * x) + (((cosD3 * cosD2) - (d7 * sinD2)) * y) + ((-sinD3 * sinD4) * z),
                ((cosD2 * sinD4) * x) + (((-sinD4) * sinD2) * y) + (cosD4 * z));
    }

    private static double sin(double d) {
        return Math.sin(d / 180.0 * Math.PI);
    }

    private static double cos(double d) {
        return Math.cos(d / 180.0 * Math.PI);
    }

    private static double atan(double d) {
        return Math.atan(d) * 180.0 / Math.PI;
    }

    private static double atan2(double y, double x) {
        double degrees = Math.atan2(y, x) * 180.0 / Math.PI;
        return (degrees < 0.0) ? (degrees + 360.0) : degrees;
    }
}