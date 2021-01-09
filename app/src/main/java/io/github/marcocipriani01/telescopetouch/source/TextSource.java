package io.github.marcocipriani01.telescopetouch.source;

/**
 * This interface corresponds to a text label placed at some fixed location in
 * space.
 *
 * @author Brent Bryan
 */
public interface TextSource extends Colorable, PositionSource {

    /**
     * Returns the text to be displayed at the specified location in the renderer.
     */
    String getText();

    /**
     * Changes the text in this {@link TextSource}.
     */
    void setText(String newText);

    /**
     * Returns the size of the font in points (e.g. 10, 12).
     */
    int getFontSize();

    float getOffset();
    // TODO(brent): talk to James: can we add font, style info?
    // TODO(brent): can we specify label orientation?
}
