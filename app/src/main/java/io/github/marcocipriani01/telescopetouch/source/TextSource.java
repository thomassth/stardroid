/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.source;

import java.util.Objects;

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;


/**
 * A Source which consists of only a text label (no point will be drawn).
 *
 * @author Brent Bryan
 */
public class TextSource extends AbstractSource implements Colorable, PositionSource {

    public final float offset;
    public final int fontSize;
    public String label;

    public TextSource(float ra, float dec, String label, int color) {
        this(GeocentricCoordinates.getInstance(ra, dec), label, color);
    }

    public TextSource(GeocentricCoordinates coords, String label, int color) {
        this(coords, label, color, 0.02f, 15);
    }

    public TextSource(GeocentricCoordinates coords, String label, int color, float offset, int fontSize) {
        super(coords, color);
        this.label = Objects.requireNonNull(label);
        if (label.trim().isEmpty()) throw new IllegalArgumentException();
        this.offset = offset;
        this.fontSize = fontSize;
    }

    /**
     * Returns the text to be displayed at the specified location in the renderer.
     */
    public String getText() {
        return label;
    }

    /**
     * Changes the text in this {@link TextSource}.
     */
    public void setText(String newText) {
        label = newText;
    }

    /**
     * Returns the size of the font in points (e.g. 10, 12).
     */
    public int getFontSize() {
        return fontSize;
    }

    public float getOffset() {
        return offset;
    }
}