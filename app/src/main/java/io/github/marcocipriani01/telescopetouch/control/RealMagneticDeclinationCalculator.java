package io.github.marcocipriani01.telescopetouch.control;

import android.hardware.GeomagneticField;

import io.github.marcocipriani01.telescopetouch.units.LatLong;

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
        if (geomagneticField == null) {
            return 0;
        }
        return geomagneticField.getDeclination();
    }

    /**
     * Sets the user's current location and time.
     */
    @Override
    public void setLocationAndTime(LatLong location, long timeInMillis) {
        geomagneticField = new GeomagneticField(location.getLatitude(),
                location.getLongitude(),
                0,
                timeInMillis);
    }

    @Override
    public String toString() {
        return "Real Magnetic Correction";
    }
}
