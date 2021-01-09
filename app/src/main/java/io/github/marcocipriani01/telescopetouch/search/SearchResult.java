package io.github.marcocipriani01.telescopetouch.search;

import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * A single search result.
 *
 * @author John Taylor
 */
public class SearchResult {
    /**
     * The coordinates of the object.
     */
    public GeocentricCoordinates coords;
    /**
     * The user-presentable name of the object, properly capitalized.
     */
    public String capitalizedName;

    /**
     * @param capitalizedName The user-presentable name of the object, properly capitalized.
     * @param coords          The coordinates of the object.
     */
    public SearchResult(String capitalizedName, GeocentricCoordinates coords) {
        this.capitalizedName = capitalizedName;
        this.coords = coords;
    }

    @Override
    public String toString() {
        return capitalizedName;
    }
}
