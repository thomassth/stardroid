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

package io.github.marcocipriani01.telescopetouch.source;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;

/**
 * For representing constellations, constellation boundaries etc.
 */
public class LineSource extends AbstractSource implements Colorable {

    public final List<GeocentricCoordinates> vertices;
    public final List<EquatorialCoordinates> raDecs;
    public final float lineWidth;

    public LineSource() {
        this(Color.WHITE, new ArrayList<>(), 1.5f);
    }

    public LineSource(int color) {
        this(color, new ArrayList<>(), 1.5f);
    }

    public LineSource(int color, List<GeocentricCoordinates> vertices, float lineWidth) {
        super(color);

        this.vertices = vertices;
        this.raDecs = new ArrayList<>();
        this.lineWidth = lineWidth;
    }

    /**
     * Returns the width of the line to be drawn.
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Returns an ordered list of the vertices which should be used to draw a
     * polyline in the renderer.
     */
    public List<GeocentricCoordinates> getVertices() {
        List<GeocentricCoordinates> result;
        if (vertices != null) {
            result = vertices;
        } else {
            result = new ArrayList<>();
        }
        return Collections.unmodifiableList(result);
    }
}