package io.github.marcocipriani01.telescopetouch.source;

import android.graphics.Bitmap;

/**
 * This source corresponds to an image to be drawn at a specific point on the
 * sky by the renderer.
 *
 * @author Brent Bryan
 */
public interface ImageSource extends PositionSource {

    /**
     * Returns the image to be displayed at the specified point.
     */
    Bitmap getImage();

    // TODO(brent): talk to James to determine what's really needed here.

    float[] getVerticalCorner();

    float[] getHorizontalCorner();

    boolean requiresBlending();
}
