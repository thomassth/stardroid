package io.github.marcocipriani01.telescopetouch.source;

/**
 * This interface corresponds to an object which can be represented by a single
 * point in space, such as a star.
 *
 * @author Brent Bryan
 */
public interface PointSource extends Colorable, PositionSource {
    /**
     * Returns the size of the dot which should be drawn to represent this point
     * in the renderer.
     */
    int getSize();

    /**
     * Returns the Shape of the image used to render the point in the texture file.
     */
    Shape getPointShape();

    enum Shape {
        CIRCLE(),
        STAR(),
        ELLIPTICAL_GALAXY(),
        SPIRAL_GALAXY(),
        IRREGULAR_GALAXY(),
        LENTICULAR_GALAXY(),
        GLOBULAR_CLUSTER(),
        OPEN_CLUSTER(),
        NEBULA(),
        HUBBLE_DEEP_FIELD();

        public int getImageIndex() {
            // return imageIndex;
            return 0;
        }
    }
}