package io.github.marcocipriani01.telescopetouch.source.impl;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.RaDec;

/**
 * For representing constellations, constellation boundaries etc.
 */
public class LineSourceImpl extends AbstractSource implements LineSource {

    public final List<GeocentricCoordinates> vertices;
    public final List<RaDec> raDecs;
    public final float lineWidth;

    public LineSourceImpl() {
        this(Color.WHITE, new ArrayList<>(), 1.5f);
    }

    public LineSourceImpl(int color) {
        this(color, new ArrayList<>(), 1.5f);
    }

    public LineSourceImpl(int color, List<GeocentricCoordinates> vertices, float lineWidth) {
        super(color);

        this.vertices = vertices;
        this.raDecs = new ArrayList<>();
        this.lineWidth = lineWidth;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public List<GeocentricCoordinates> getVertices() {
        List<GeocentricCoordinates> result;
        if (vertices != null) {
            result = vertices;
        } else {
            result = new ArrayList<>();
        }
        return Collections.unmodifiableList(result);
    }
}