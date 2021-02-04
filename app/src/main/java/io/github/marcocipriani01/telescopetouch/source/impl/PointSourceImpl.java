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

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.source.PointSource;

/**
 * This class represents a astronomical point source, such as a star, or a distant galaxy.
 *
 * @author Brent Bryan
 */
public class PointSourceImpl extends AbstractSource implements PointSource {

    public final int size;
    private final Shape pointShape;

    public PointSourceImpl(float ra, float dec, int color, int size) {
        this(GeocentricCoordinates.getInstance(ra, dec), color, size);
    }

    public PointSourceImpl(GeocentricCoordinates coords, int color, int size) {
        this(coords, color, size, Shape.CIRCLE);
    }

    public PointSourceImpl(GeocentricCoordinates coords, int color, int size, Shape pointShape) {
        super(coords, color);
        this.size = size;
        this.pointShape = pointShape;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Shape getPointShape() {
        return pointShape;
    }
}