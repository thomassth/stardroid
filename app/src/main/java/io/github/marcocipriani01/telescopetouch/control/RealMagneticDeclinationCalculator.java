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

package io.github.marcocipriani01.telescopetouch.control;

import android.hardware.GeomagneticField;
import android.location.Location;

import androidx.annotation.NonNull;

/**
 * Encapsulates the calculation of magnetic declination for the user's location
 * and position.
 *
 * @author John Taylor
 */
public class RealMagneticDeclinationCalculator implements MagneticDeclinationCalculator {

    private GeomagneticField geomagneticField;

    /**
     * {@inheritDoc}
     * Silently returns zero if the time and location have not been set.
     */
    @Override
    public float getDeclination() {
        if (geomagneticField == null) return 0.0f;
        return geomagneticField.getDeclination();
    }

    /**
     * Sets the user's current location and time.
     */
    @Override
    public void setLocationAndTime(Location location, long timeInMillis) {
        geomagneticField = new GeomagneticField((float) location.getLatitude(),
                (float) location.getLongitude(), (float) location.getAltitude(), timeInMillis);
    }

    @NonNull
    @Override
    public String toString() {
        return "Real Magnetic Correction";
    }
}