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

package io.github.marcocipriani01.telescopetouch.units;

import androidx.annotation.NonNull;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.astronomy.Planet;
import io.github.marcocipriani01.telescopetouch.util.MathsUtils;

public class RaDec {

    public float ra;        // In degrees
    public float dec;       // In degrees

    public RaDec(float ra, float dec) {
        this.ra = ra;
        this.dec = dec;
    }

    public static RaDec calculateRaDecDist(HeliocentricCoordinates coords) {
        // find the RA and DEC from the rectangular equatorial coords
        float ra = MathsUtils.mod2pi((float) Math.atan2(coords.y, coords.x)) * MathsUtils.RADIANS_TO_DEGREES;
        float dec = (float) (Math.atan(coords.z / Math.sqrt(coords.x * coords.x + coords.y * coords.y))
                * MathsUtils.RADIANS_TO_DEGREES);

        return new RaDec(ra, dec);
    }

    public static RaDec getInstance(Planet planet, Calendar time, HeliocentricCoordinates earthCoordinates) {
        // TODO(serafini): This is a temporary hack until we re-factor the Planetary calculations.
        if (planet.equals(Planet.Moon)) {
            return Planet.calculateLunarGeocentricLocation(time);
        }
        HeliocentricCoordinates coords;
        if (planet.equals(Planet.Sun)) {
            // Invert the view, since we want the Sun in earth coordinates, not the Earth in sun
            // coordinates.
            coords = new HeliocentricCoordinates(earthCoordinates.radius, earthCoordinates.x * -1.0f,
                    earthCoordinates.y * -1.0f, earthCoordinates.z * -1.0f);
        } else {
            coords = HeliocentricCoordinates.getInstance(planet, time);
            coords.Subtract(earthCoordinates);
        }
        HeliocentricCoordinates equ = coords.CalculateEquatorialCoordinates();
        return calculateRaDecDist(equ);
    }

    public static RaDec getInstance(GeocentricCoordinates coords) {
        float raRad = (float) Math.atan2(coords.y, coords.x);
        if (raRad < 0) raRad += 2f * (float) Math.PI;
        float decRad = (float) Math.atan2(coords.z, Math.sqrt(coords.x * coords.x + coords.y * coords.y));
        return new RaDec(raRad * MathsUtils.RADIANS_TO_DEGREES, decRad * MathsUtils.RADIANS_TO_DEGREES);
    }

    @NonNull
    @Override
    public String toString() {
        return "RA: " + ra + " degrees\n" +
                "Dec: " + dec + " degrees\n";
    }
}