package io.github.marcocipriani01.telescopetouch.units;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.ephemeris.OrbitalElements;
import io.github.marcocipriani01.telescopetouch.ephemeris.Planet;
import io.github.marcocipriani01.telescopetouch.util.Geometry;

public class HeliocentricCoordinates extends Vector3 {

    // Value of the obliquity of the ecliptic for J2000
    private static final float OBLIQUITY = 23.439281f * Geometry.DEGREES_TO_RADIANS;
    public final float radius;  // Radius. (AU)

    public HeliocentricCoordinates(float radius, float xh, float yh, float zh) {
        super(xh, yh, zh);
        this.radius = radius;
    }

    public static HeliocentricCoordinates getInstance(Planet planet, Calendar date) {
        return getInstance(planet.getOrbitalElements(date));
    }

    public static HeliocentricCoordinates getInstance(OrbitalElements elem) {
        float anomaly = elem.getAnomaly();
        float ecc = elem.eccentricity;
        float radius = elem.distance * (1 - ecc * ecc) / (1 + ecc * (float) Math.cos(anomaly));

        // heliocentric rectangular coordinates of planet
        float per = elem.perihelion;
        float asc = elem.ascendingNode;
        float inc = elem.inclination;
        float cos = (float) Math.cos(anomaly + per - asc);
        float sin = (float) Math.sin(anomaly + per - asc);
        float xh = radius *
                ((float) Math.cos(asc) * cos -
                        (float) Math.sin(asc) * sin *
                                (float) Math.cos(inc));
        float yh = radius *
                ((float) Math.sin(asc) * cos +
                        (float) Math.cos(asc) * sin *
                                (float) Math.cos(inc));
        float zh = radius * (sin * (float) Math.sin(inc));

        return new HeliocentricCoordinates(radius, xh, yh, zh);
    }

    /**
     * Subtracts the values of the given heliocentric coordinates from this
     * object.
     */
    public void Subtract(HeliocentricCoordinates other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
    }

    public HeliocentricCoordinates CalculateEquatorialCoordinates() {
        return new HeliocentricCoordinates(this.radius, this.x,
                (float) (this.y * Math.cos(OBLIQUITY) - this.z * Math.sin(OBLIQUITY)),
                (float) (this.y * Math.sin(OBLIQUITY) + this.z * Math.cos(OBLIQUITY)));
    }

    public float DistanceFrom(HeliocentricCoordinates other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%f, %f, %f, %f)", x, y, z, radius);
    }
}