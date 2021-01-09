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
        CIRCLE(0),
        STAR(1),
        ELLIPTICAL_GALAXY(2),
        SPIRAL_GALAXY(3),
        IRREGULAR_GALAXY(4),
        LENTICULAR_GALAXY(3),
        GLOBULAR_CLUSTER(5),
        OPEN_CLUSTER(6),
        NEBULA(7),
        HUBBLE_DEEP_FIELD(8);

        private final int imageIndex;

        Shape(int imageIndex) {
            this.imageIndex = imageIndex;
        }

        public int getImageIndex() {
            // return imageIndex;
            return 0;
        }
    }
}
