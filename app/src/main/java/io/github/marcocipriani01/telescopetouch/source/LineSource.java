package io.github.marcocipriani01.telescopetouch.source;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * This interface corresponds to a set of successive line segments (drawn from
 * consecutive vertices). That is, for the vertices {A, B, C, D}, lines should
 * be drawn between A and B, B and C, and C and D.
 *
 * @author Brent Bryan
 */
public interface LineSource extends Colorable {

    /**
     * Returns the width of the line to be drawn.
     */
    float getLineWidth();

    // TODO(brent): Discuss with James to add solid, dashed, dotted, etc.

    /**
     * Returns an ordered list of the vertices which should be used to draw a
     * polyline in the renderer.
     */
    List<GeocentricCoordinates> getVertices();
}
