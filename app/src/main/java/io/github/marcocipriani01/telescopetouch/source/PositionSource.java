package io.github.marcocipriani01.telescopetouch.source;

import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * This interface corresponds to sources which are located at a singular fixed
 * point in the sky, such as stars and planets.
 *
 * @author Brent Bryan
 */
public interface PositionSource {

    /**
     * Returns the location of the source in Geocentric Euclidean coordinates.
     */
    GeocentricCoordinates getLocation();
}
