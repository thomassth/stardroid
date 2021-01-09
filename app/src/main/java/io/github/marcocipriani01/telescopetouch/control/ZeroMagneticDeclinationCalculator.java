package io.github.marcocipriani01.telescopetouch.control;

import io.github.marcocipriani01.telescopetouch.units.LatLong;

/**
 * A trivial calculator that returns zero magnetic declination.  Used when
 * the user does not want magnetic correction.
 *
 * @author John Taylor
 */
public final class ZeroMagneticDeclinationCalculator implements MagneticDeclinationCalculator {

    @Override
    public float getDeclination() {
        return 0;
    }

    @Override
    public void setLocationAndTime(LatLong location, long timeInMills) {
        // Do nothing.
    }

    @Override
    public String toString() {
        return "Zero Magnetic Correction";
    }
}