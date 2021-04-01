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

package io.github.marcocipriani01.telescopetouch.search;

import androidx.annotation.NonNull;

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;

/**
 * A single search result.
 *
 * @author John Taylor
 */
public class SearchResult {
    /**
     * The coordinates of the object.
     */
    public final GeocentricCoordinates coords;
    /**
     * The user-presentable name of the object, properly capitalized.
     */
    public final String capitalizedName;

    /**
     * @param capitalizedName The user-presentable name of the object, properly capitalized.
     * @param coords          The coordinates of the object.
     */
    public SearchResult(String capitalizedName, GeocentricCoordinates coords) {
        this.capitalizedName = capitalizedName;
        this.coords = coords;
    }

    @NonNull
    @Override
    public String toString() {
        return capitalizedName;
    }
}