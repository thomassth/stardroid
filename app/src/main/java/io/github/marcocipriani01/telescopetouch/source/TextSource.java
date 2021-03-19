/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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