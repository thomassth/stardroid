package io.github.marcocipriani01.telescopetouch.source;

/**
 * This interface indicates that a Source has a color associated with it.
 *
 * @author Brent Bryan
 */
public interface Colorable {

    /**
     * Returns the color (as an Android Color int) associated with the given
     * object.
     */
    int getColor();
}
