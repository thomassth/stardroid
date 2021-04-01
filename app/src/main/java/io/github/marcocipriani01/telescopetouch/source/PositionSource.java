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
 * This interface corresponds to sources which are located at a singular fixed
 * point in the sky, such as stars and planets.
 *
 * @author Brent Bryan
 */
public interface PositionSource {

    /**
     * Returns the location of the source in Geocentric Euclidean coordinates.
     */
    GeocentricCoordinates getLocation();
}