package io.github.marcocipriani01.telescopetouch.astronomy;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.maths.Formatters;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class HorizontalCoordinates {

    public double alt;
    public double az;

    public HorizontalCoordinates(double alt, double az) {
        this.alt = alt;
        this.az = az;
    }

    public static HorizontalCoordinates getInstance(EquatorialCoordinates eq, Location observer, Calendar time) {
        return getInstance(eq, observer.getLatitude(), TimeUtils.meanSiderealTime(time, observer.getLongitude()));
    }

    public static boolean isObjectAboveHorizon(EquatorialCoordinates eq, double latitude, double siderealTime) {
        double latRadians = toRadians(latitude),
                dec = toRadians(eq.dec),
                alt = asin(sin(dec) * sin(latRadians) + cos(dec) * cos(latRadians) * cos(toRadians(siderealTime - eq.ra)));
        return alt > 0.0;
    }

    public static HorizontalCoordinates getInstance(EquatorialCoordinates eq, double latitude, double siderealTime) {
        double latRadians = toRadians(latitude),
                dec = toRadians(eq.dec),
                ha = toRadians(siderealTime - eq.ra),
                alt = asin(sin(dec) * sin(latRadians) + cos(dec) * cos(latRadians) * cos(ha));
        return new HorizontalCoordinates(toDegrees(alt),
                toDegrees(atan2(-sin(ha) * cos(dec) / cos(alt), (sin(dec) - sin(latRadians) * sin(alt)) / (cos(latRadians) * cos(alt)))));
    }

    public String getAltString() {
        return Formatters.formatDegrees(alt);
    }

    public String getAzString() {
        return Formatters.formatDegrees(az);
    }

    @NonNull
    @Override
    public String toString() {
        return "Alt: " + getAltString() + ", Az: " + getAzString();
    }
}