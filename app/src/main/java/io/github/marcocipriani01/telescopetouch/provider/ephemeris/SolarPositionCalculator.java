package io.github.marcocipriani01.telescopetouch.provider.ephemeris;

import java.util.Date;

import io.github.marcocipriani01.telescopetouch.units.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.RaDec;

/**
 * Calculate the position of the Sun in RA and Dec
 * <p>
 * TODO(johntaylor): get rid of this class once the provider
 * framework is refactored.  This duplicates functionality from elsewhere,
 * but the current ephemeris/provider code is a bit too tangled up for easy reuse.
 */
public class SolarPositionCalculator {
    public static RaDec getSolarPosition(Date time) {
        HeliocentricCoordinates sunCoordinates = HeliocentricCoordinates.getInstance(Planet.Sun, time);
        return RaDec.getInstance(Planet.Sun, time, sunCoordinates);
    }
}
