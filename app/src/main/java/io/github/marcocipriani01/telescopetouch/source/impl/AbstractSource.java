package io.github.marcocipriani01.telescopetouch.source.impl;

import android.graphics.Color;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.source.Colorable;
import io.github.marcocipriani01.telescopetouch.source.PositionSource;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * This class represents the base of an astronomical object to be
 * displayed by the UI.  These object need not be only stars and
 * galaxies but also include labels (such as the name of major stars)
 * and constellation depictions.
 *
 * @author Brent Bryan
 */
public abstract class AbstractSource implements Colorable, PositionSource {
    private final int color;
    private final GeocentricCoordinates xyz;
    public UpdateGranularity granularity;
    private List<String> names;

    @Deprecated
    AbstractSource() {
        this(GeocentricCoordinates.getInstance(0.0f, 0.0f), Color.BLACK);
    }

    protected AbstractSource(int color) {
        this(GeocentricCoordinates.getInstance(0.0f, 0.0f), color);
    }

    protected AbstractSource(GeocentricCoordinates coords, int color) {
        this.xyz = coords;
        this.color = color;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public GeocentricCoordinates getLocation() {
        return xyz;
    }

    /**
     * Each source has an update granularity associated with it, which
     * defines how often it's provider expects its value to change.
     */
    public enum UpdateGranularity {
        Second, Minute, Hour, Day, Year
    }
}