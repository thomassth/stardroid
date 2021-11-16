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

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.math.getGeocentricCoords

/**
 * A celestial object represented by an image, such as a planet or a
 * galaxy.
 */
class ImagePrimitive(
    coords: Vector3, protected val resources: Resources?, id: Int, upVec: Vector3?,
    private val imageScale: Float
) : AbstractPrimitive(coords, Color.WHITE) {
    // These two vectors, along with Source.xyz, determine the position of the
    // image object.  The corners are as follows
    //
    //  xyz-u+v   xyz+u+v
    //     +---------+     ^
    //     |   xyz   |     | v
    //     |    .    |     .
    //     |         |
    //     +---------+
    //  xyz-u-v    xyz+u-v
    //
    //          .--->
    //            u
    var ux = 0f
    var uy = 0f
    var uz = 0f
    var vx = 0f
    var vy = 0f
    var vz = 0f
    var image: Bitmap? = null
    var requiresBlending = false

    @JvmOverloads
    constructor(
        ra: Float, dec: Float, res: Resources?, id: Int, upVec: Vector3? = up,
        imageScale: Float = 1.0f
    ) : this(getGeocentricCoords(ra, dec), res, id, upVec, imageScale) {
    }

    fun setImageId(imageId: Int) {
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        image = BitmapFactory.decodeResource(resources, imageId, opts)
        if (image == null) {
            throw RuntimeException("Coud not decode image $imageId")
        }
    }

    val horizontalCorner: FloatArray
        get() = floatArrayOf(ux, uy, uz)
    val verticalCorner: FloatArray
        get() = floatArrayOf(vx, vy, vz)

    fun requiresBlending(): Boolean {
        return requiresBlending
    }

    fun setUpVector(upVec: Vector3?) {
        val p = this.location
        val u = p.times(upVec!!).normalizedCopy().unaryMinus()
        val v = u.times(p)
        v.timesAssign(imageScale)
        u.timesAssign(imageScale)

        // TODO(serafini): Can we replace these with a float[]?
        ux = u.x
        uy = u.y
        uz = u.z
        vx = v.x
        vy = v.y
        vz = v.z
    }

    companion object {
        var up = Vector3(0.0f, 1.0f, 0.0f)
    }

    init {

        // TODO(jpowell): We're never freeing this resource, so we leak it every
        // time we create a new ImagePrimitive and garbage collect an old one.
        // We need to make sure it gets freed.
        // We should also cache this so we don't have to keep reloading these
        // which is really slow and adds noticeable lag to the application when it
        // happens.
        setUpVector(upVec)
        setImageId(id)
    }
}