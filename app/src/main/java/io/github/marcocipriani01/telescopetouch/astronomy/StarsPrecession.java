package io.github.marcocipriani01.telescopetouch.astronomy;

import android.util.Log;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class StarsPrecession {

    private static final String TAG = TelescopeTouchApp.getTag(StarsPrecession.class);
    private double j2000DEC = 89.26410897222222d;
    private double j2000RA = 37.954560666666666d;
    private double precessedDec;
    private double precessedRa;

    public StarsPrecession(boolean northern) {
        if (!northern) {
            this.j2000RA = 317.1951809166667d;
            this.j2000DEC = -88.95650324722223d;
        }
        double[] result = calculatePrecession(this.j2000RA, this.j2000DEC);
        this.precessedRa = result[0];
        this.precessedDec = result[1];
        Log.d(TAG, "J2000 RA: " + this.j2000RA);
        Log.d(TAG, "J2000 DEC: " + this.j2000DEC);
        Log.d(TAG, "Calculated precession: " + this.precessedRa / 15.0d);
    }

    private static double[] calculatePrecession(double ra, double dec) {
        double x = cos(dec) * cos(ra);
        double y = cos(dec) * sin(ra);
        double z = sin(dec);
        Calendar calendar = Calendar.getInstance();
        double d1 = ((((double) calendar.get(Calendar.YEAR)) + (((double) calendar.get(Calendar.DAY_OF_YEAR)) / 365.0d)) - 2000.0d) / 100.0d,
                d2 = (((((0.017998d * d1) + 0.30188d) * d1) + 2306.2181d) * d1) / 3600.0d,
                d3 = (((((2.05E-4d * d1) + 0.7928d) * d1) * d1) / 3600.0d) + d2,
                d4 = ((2004.3109d - (((0.041833d * d1) + 0.42665d) * d1)) * d1) / 3600.0d,
                cosD2 = cos(d2),
                cosD3 = cos(d3),
                cosD4 = cos(d4),
                sinD2 = sin(d2),
                sinD3 = sin(d3),
                sinD4 = sin(d4),
                d6 = cosD3 * cosD4,
                d7 = sinD3 * cosD4;
        x = (((-sinD3 * sinD2) + (d6 * cosD2)) * x) + (((-sinD3 * cosD2) - (d6 * sinD2)) * y) + (((-cosD3) * sinD4) * z);
        y = (((cosD3 * sinD2) + (d7 * cosD2)) * x) + (((cosD3 * cosD2) - (d7 * sinD2)) * y) + ((-sinD3 * sinD4) * z);
        z = ((cosD2 * sinD4) * x) + (((-sinD4) * sinD2) * y) + (cosD4 * z);
        return new double[]{atan2(y, x), atan(z / Math.sqrt((x * x) + (y * y)))};
    }

    private static double sin(double d) {
        return Math.sin(Math.toRadians(d));
    }

    private static double cos(double d) {
        return Math.cos(Math.toRadians(d));
    }

    private static double atan(double d) {
        return Math.toDegrees(Math.atan(d));
    }

    private static double atan2(double y, double x) {
        double degrees = Math.toDegrees(Math.atan2(y, x));
        return (degrees < 0.0d) ? (degrees + 360.0d) : degrees;
    }

    public double getPrecessedDec() {
        return precessedDec;
    }

    public double getPrecessedRA() {
        return precessedRa;
    }
}