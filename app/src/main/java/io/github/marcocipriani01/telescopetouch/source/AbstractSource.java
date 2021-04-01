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

import android.graphics.Color;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;

/**
 * This class represents the base of an astronomical object to be
 * displayed by the UI.  These object need not be only stars and
 * galaxies but also include labels (such as the name of major stars)
 * and constellation depictions.
 *
 * @author Brent Bryan
 */
public abstract class AbstractSource implements Colorable, PositionSource {

    private final int color;
    private final GeocentricCoordinates xyz;
    private List<String> names;

    @Deprecated
    AbstractSource() {
        this(GeocentricCoordinates.getInstance(0.0f, 0.0f), Color.BLACK);
    }

    protected AbstractSource(int color) {
        this(GeocentricCoordinates.getInstance(0.0f, 0.0f), color);
    }

    protected AbstractSource(GeocentricCoordinates coords, int color) {
        this.xyz = coords;
        this.color = color;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public GeocentricCoordinates getLocation() {
        return xyz;
    }
}