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
package com.google.android.stardroid.source

import android.graphics.Color
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.math.getGeocentricCoords

/** This class represents the base of an astronomical object to be
 * displayed by the UI.  These object need not be only stars and
 * galaxies but also include labels (such as the name of major stars)
 * and constellation depictions.
 *
 * @author Brent Bryan
 */
abstract class AbstractPrimitive protected constructor(val location: Vector3, val color: Int) {
    /** Each source has an update granularity associated with it, which
     * defines how often it's provider expects its value to change.
     */
    enum class UpdateGranularity {
        Second, Minute, Hour, Day, Year
    }

    var granularity: UpdateGranularity? = null
    var names: List<String>? = null

    @Deprecated("")
    internal constructor() : this(getGeocentricCoords(0.0f, 0.0f), Color.BLACK) {
    }

    protected constructor(color: Int) : this(getGeocentricCoords(0.0f, 0.0f), color) {}
}