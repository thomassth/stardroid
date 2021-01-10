package io.github.marcocipriani01.telescopetouch.source.impl;

import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * This class represents a astronomical point source, such as a star, or a distant galaxy.
 *
 * @author Brent Bryan
 */

public class PointSourceImpl extends AbstractSource implements PointSource {

    public final int size;
    private final Shape pointShape;

    public PointSourceImpl(float ra, float dec, int color, int size) {
        this(GeocentricCoordinates.getInstance(ra, dec), color, size);
    }

    public PointSourceImpl(GeocentricCoordinates coords, int color, int size) {
        this(coords, color, size, Shape.CIRCLE);
    }

    public PointSourceImpl(GeocentricCoordinates coords, int color, int size, Shape pointShape) {
        super(coords, color);
        this.size = size;
        this.pointShape = pointShape;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Shape getPointShape() {
        return pointShape;
    }
}
