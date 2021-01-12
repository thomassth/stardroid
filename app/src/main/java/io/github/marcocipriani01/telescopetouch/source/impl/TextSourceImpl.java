/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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

package io.github.marcocipriani01.telescopetouch.source.impl;

import java.util.Objects;

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
        this.label = Objects.requireNonNull(label);
        if (label.trim().isEmpty()) throw new IllegalArgumentException();

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