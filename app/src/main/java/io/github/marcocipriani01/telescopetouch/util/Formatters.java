package io.github.marcocipriani01.telescopetouch.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

public class Formatters {

    private Formatters() {
    }

    @SuppressLint("DefaultLocale")
    public static String angleToString(double angle, boolean degOrHourFormat) {
        int deg = (int) angle;
        double tmp = (angle % 1.0) * 3600.0;
        int min = Math.abs((int) (tmp / 60.0));
        int sec = Math.abs((int) (tmp % 60.0));
        if (degOrHourFormat) {
            return String.format("%1$02d°%2$02d'%3$02d\"", deg, min, sec);
        } else {
            return String.format("%1$02dh%2$02dm%3$02ds", deg, min, sec);
        }
    }

    public static String latitudeToString(double latitude, Context context) {
        return angleToString(Math.abs(latitude), true) + " " +
                (latitude >= 0.0 ? context.getString(R.string.north_short) : context.getString(R.string.south_short));
    }

    public static String longitudeToString(double longitude, Context context) {
        return angleToString(Math.abs(longitude), true) + " " +
                (longitude >= 0.0 ? context.getString(R.string.east_short) : context.getString(R.string.west_short));
    }

    @SuppressLint("DefaultLocale")
    public static String declinationToString(float declination, Context context) {
        return String.format("%.2f", Math.abs(declination)) + "° " +
                ((declination > 0) ? context.getString(R.string.east_short) : context.getString(R.string.west_short));
    }

    /**
     * Angular distance between the two points.
     *
     * @return degrees
     */
    public static float locationDistance(Location one, Location two) {
        // Some misuse of the astronomy math classes
        GeocentricCoordinates otherPnt = GeocentricCoordinates.getInstance(two.getLongitude(), two.getLatitude());
        GeocentricCoordinates thisPnt = GeocentricCoordinates.getInstance(one.getLongitude(), one.getLatitude());
        float cosTheta = Vector3.cosineSimilarity(thisPnt, otherPnt);
        return (float) Math.acos(cosTheta) * 180f / (float) Math.PI;
    }
}