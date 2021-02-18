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

package io.github.marcocipriani01.telescopetouch.astronomy;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.maths.MathsUtils;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

public class HeliocentricCoordinates extends Vector3 {

    /**
     * Value of the obliquity of the ecliptic for J2000
     */
    private static final double OBLIQUITY = 23.439281 * MathsUtils.DEGREES_TO_RADIANS;
    /**
     * Radius. (AU)
     */
    public final double radius;

    public HeliocentricCoordinates(double radius, double xh, double yh, double zh) {
        super(xh, yh, zh);
        this.radius = radius;
    }

    public static HeliocentricCoordinates getInstance(Planet planet, Calendar date) {
        return getInstance(planet.getOrbitalElements(date));
    }

    public static HeliocentricCoordinates getInstance(OrbitalElements elem) {
        double anomaly = elem.getAnomaly(),
                ecc = elem.eccentricity,
                radius = elem.distance * (1.0 - ecc * ecc) / (1.0 + ecc * Math.cos(anomaly));
        // heliocentric rectangular coordinates of planet
        double per = elem.perihelion,
                asc = elem.ascendingNode,
                inc = elem.inclination,
                cos = Math.cos(anomaly + per - asc),
                sin = Math.sin(anomaly + per - asc);
        return new HeliocentricCoordinates(radius, radius * ((float) Math.cos(asc) * cos - Math.sin(asc) * sin * Math.cos(inc)),
                radius * (Math.sin(asc) * cos + Math.cos(asc) * sin * Math.cos(inc)),
                radius * (sin * Math.sin(inc)));
    }

    /**
     * Subtracts the values of the given heliocentric coordinates from this
     * object.
     */
    public void subtract(HeliocentricCoordinates other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
    }

    public HeliocentricCoordinates calculateEquatorialCoordinates() {
        return new HeliocentricCoordinates(this.radius, this.x,
                (this.y * Math.cos(OBLIQUITY) - this.z * Math.sin(OBLIQUITY)),
                (this.y * Math.sin(OBLIQUITY) + this.z * Math.cos(OBLIQUITY)));
    }

    public double distanceFrom(HeliocentricCoordinates other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%f, %f, %f, %f)", x, y, z, radius);
    }
}