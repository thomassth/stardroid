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

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;

/**
 * This class represents a astronomical point source, such as a star, or a distant galaxy.
 *
 * @author Brent Bryan
 */
public class PointSource extends AbstractSource implements Colorable, PositionSource {

    public final int size;

    public PointSource(GeocentricCoordinates coords, int color, int size) {
        super(coords, color);
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}