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