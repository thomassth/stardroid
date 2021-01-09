package io.github.marcocipriani01.telescopetouch.control;

import io.github.marcocipriani01.telescopetouch.units.LatLong;

/**
 * Passed to the {@link AstronomerModel} to calculate the local magnetic
 * declination.
 *
 * @author John Taylor
 */
public interface MagneticDeclinationCalculator {

    /**
     * Returns the magnetic declination in degrees, that is, the rotation between
     * magnetic North and true North.
     */
    float getDeclination();

    /**
     * Sets the user's location and time.
     */
    void setLocationAndTime(LatLong location, long timeInMillis);
}
