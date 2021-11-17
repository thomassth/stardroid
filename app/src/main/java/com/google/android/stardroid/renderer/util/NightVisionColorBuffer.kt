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
package com.google.android.stardroid.renderer.util

import javax.microedition.khronos.opengles.GL10

/// Encapsulates a color vertex buffer where night vision can be enabled or diabled by a function call.
class NightVisionColorBuffer {
    constructor(numVertices: Int) {
        reset(numVertices)
    }

    constructor() {
        mNormalBuffer = ColorBuffer(false)
        mRedBuffer = ColorBuffer(false)
    }

    constructor(useVBO: Boolean) {
        mNormalBuffer = ColorBuffer(useVBO)
        mRedBuffer = ColorBuffer(useVBO)
    }

    fun size(): Int {
        return mNormalBuffer!!.size()
    }

    fun reset(numVertices: Int) {
        mNormalBuffer!!.reset(numVertices)
        mRedBuffer!!.reset(numVertices)
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    fun reload() {
        mNormalBuffer!!.reload()
        mRedBuffer!!.reload()
    }

    fun addColor(a: Int, r: Int, g: Int, b: Int) {
        mNormalBuffer!!.addColor(a, r, g, b)
        // I tried luminance here first, but many objects we care a lot about weren't very noticable because they were
        // bluish.  An average gets a better result.
        val avg = (r + g + b) / 3
        mRedBuffer!!.addColor(a, avg, 0, 0)
    }

    fun addColor(abgr: Int) {
        val a = abgr shr 24 and 0xff
        val b = abgr shr 16 and 0xff
        val g = abgr shr 8 and 0xff
        val r = abgr and 0xff
        addColor(a, r, g, b)
    }

    operator fun set(gl: GL10, nightVisionMode: Boolean) {
        if (nightVisionMode) {
            mRedBuffer!!.set(gl)
        } else {
            mNormalBuffer!!.set(gl)
        }
    }

    private var mNormalBuffer: ColorBuffer? = null
    private var mRedBuffer: ColorBuffer? = null
}