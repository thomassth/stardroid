// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.source

import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.math.getGeocentricCoords
import com.google.common.base.Preconditions

/**
 * A Primitive which consists of only a text label (no point will be drawn).
 *
 * @author Brent Bryan
 */
class TextPrimitive @JvmOverloads constructor(
    coords: Vector3, label: String, color: Int, offset: Float = 0.02f,
    fontSize: Int = 15
) : AbstractPrimitive(coords, color) {
    var text: String? = null
    val offset: Float
    val fontSize: Int

    constructor(ra: Float, dec: Float, label: String, color: Int) : this(
        getGeocentricCoords(
            ra,
            dec
        ), label, color
    ) {
    }

    init {
        text = Preconditions.checkNotNull(label)
        Preconditions.checkArgument(!label.trim { it <= ' ' }.isEmpty())
        this.offset = offset
        this.fontSize = fontSize
    }
}