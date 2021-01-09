package io.github.marcocipriani01.telescopetouch.source.impl;

import com.google.common.base.Preconditions;

import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;


/**
 * A Source which consists of only a text label (no point will be drawn).
 *
 * @author Brent Bryan
 */
public class TextSourceImpl extends AbstractSource implements TextSource {
    public final float offset;
    public final int fontSize;
    public String label;

    public TextSourceImpl(float ra, float dec, String label, int color) {
        this(GeocentricCoordinates.getInstance(ra, dec), label, color);
    }

    public TextSourceImpl(GeocentricCoordinates coords, String label, int color) {
        this(coords, label, color, 0.02f, 15);
    }

    public TextSourceImpl(GeocentricCoordinates coords, String label, int color, float offset,
                          int fontSize) {

        super(coords, color);
        this.label = Preconditions.checkNotNull(label);
        Preconditions.checkArgument(!label.trim().isEmpty());

        this.offset = offset;
        this.fontSize = fontSize;
    }

    @Override
    public String getText() {
        return label;
    }

    @Override
    public void setText(String newText) {
        label = newText;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public float getOffset() {
        return offset;
    }
}
