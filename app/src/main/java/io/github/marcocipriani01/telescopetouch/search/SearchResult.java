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