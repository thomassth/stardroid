// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.search

import com.google.android.stardroid.math.Vector3

/**
 * A single search result.
 *
 * @author John Taylor
 */
class SearchResult
/**
 * @param capitalizedName The user-presentable name of the object, properly capitalized.
 * @param coords The geocentric coordinates of the object.
 */(
    /** The user-presentable name of the object, properly capitalized. */
    var capitalizedName: String,
    /** The coordinates of the object. */
    var coords: Vector3?
) {
    override fun toString(): String {
        return capitalizedName
    }
}