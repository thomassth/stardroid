package io.github.marcocipriani01.telescopetouch.source;

import java.util.List;

/**
 * Data object which contains all of the elements for an
 * {@link AstronomicalSource}. These elements describe the lines, text, images,
 * etc sent to renderer to be drawn.
 *
 * @author Brent Bryan
 */
public interface Sources {

    /**
     * Returns the list of points that should be drawn in the renderer.
     */
    List<? extends PointSource> getPoints();

    /**
     * Returns the list of text labels that should be drawn in the renderer.
     */
    List<? extends TextSource> getLabels();

    /**
     * Returns the list of lines that should be drawn in the renderer.
     */
    List<? extends LineSource> getLines();

    /**
     * Returns the list of images that should be drawn in the renderer.
     */
    List<? extends ImageSource> getImages();
}
