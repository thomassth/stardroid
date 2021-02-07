package io.github.marcocipriani01.telescopetouch.maths;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;

public class Formatters {

    private Formatters() {
    }

    public static String formatDegreesAsHours(double x) {
        return formatHours(x / 15.0);
    }

    @SuppressLint("DefaultLocale")
    public static String formatHours(double x) {
        double hours = Math.abs(x);
        int deg = (int) Math.floor(hours);
        double tmp = (hours - deg) * 60.0;
        int min = (int) Math.floor(tmp);
        int sec = (int) Math.round((tmp - min) * 60.0);
        if (Math.signum(x) >= 0) {
            return String.format("%02dh%02dm%02ds", deg, min, sec);
        } else {
            return String.format("-%02dh%02dm%02ds", deg, min, sec);
        }
    }

    /**
     * @return a string containing the declination (hh:mm:ss)
     */
    @SuppressLint("DefaultLocale")
    public static String formatDegrees(double x) {
        int deg = (int) Math.floor(Math.abs(x));
        int min = (int) Math.floor((Math.abs(x) - deg) * 60.0);
        int sec = (int) Math.round(((Math.abs(x) - deg) * 60.0 - min) * 60.0);
        if (Math.signum(x) >= 0) {
            return String.format("%02d°%02d'%02d\"", deg, min, sec);
        } else {
            return String.format("-%02d°%02d'%02d\"", deg, min, sec);
        }
    }

    public static String latitudeToString(double latitude, Context context) {
        return formatDegrees(Math.abs(latitude)) + " " +
                (latitude >= 0.0 ? context.getString(R.string.north_short) : context.getString(R.string.south_short));
    }

    public static String longitudeToString(double longitude, Context context) {
        return formatDegrees(Math.abs(longitude)) + " " +
                (longitude >= 0.0 ? context.getString(R.string.east_short) : context.getString(R.string.west_short));
    }

    @SuppressLint("DefaultLocale")
    public static String magDeclinationToString(float declination, Context context) {
        return String.format("%.2f", Math.abs(declination)) + "° " +
                ((declination > 0) ? context.getString(R.string.east_short) : context.getString(R.string.west_short));
    }

    /**
     * Angular distance between the two points.
     *
     * @return degrees
     */
    public static double locationDistance(Location one, Location two) {
        // Some misuse of the astronomy math classes
        GeocentricCoordinates otherPnt = GeocentricCoordinates.getInstance(two.getLongitude(), two.getLatitude());
        GeocentricCoordinates thisPnt = GeocentricCoordinates.getInstance(one.getLongitude(), one.getLatitude());
        double cosTheta = Vector3.cosineSimilarity(thisPnt, otherPnt);
        return Math.acos(cosTheta) * 180 / Math.PI;
    }
}