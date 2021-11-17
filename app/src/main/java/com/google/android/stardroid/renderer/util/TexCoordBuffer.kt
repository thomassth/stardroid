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

import android.util.Log
import com.google.android.stardroid.util.FixedPoint.floatToFixedPoint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

class TexCoordBuffer {
    constructor(numVertices: Int) {
        reset(numVertices)
    }

    // Creates an empty buffer.  Must call reset() before adding vertices.
    constructor() {
        mNumVertices = 0
    }

    constructor(useVBO: Boolean) {
        mNumVertices = 0
        mUseVBO = useVBO
    }

    fun size(): Int {
        return mNumVertices
    }

    fun reset(numVertices: Int) {
        var numVertices = numVertices
        if (numVertices < 0) {
            Log.e("TexCoordBuffer", "reset attempting to set numVertices to $numVertices")
            numVertices = 0
        }
        mNumVertices = numVertices
        regenerateBuffer()
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    fun reload() {
        mGLBuffer.reload()
    }

    fun addTexCoords(u: Float, v: Float) {
        mTexCoordBuffer!!.put(floatToFixedPoint(u))
        mTexCoordBuffer!!.put(floatToFixedPoint(v))
    }

    fun set(gl: GL10) {
        if (mNumVertices == 0) {
            return
        }
        mTexCoordBuffer!!.position(0)
        if (mUseVBO && GLBuffer.canUseVBO()) {
            val gl11 = gl as GL11
            mGLBuffer.bind(gl11, mTexCoordBuffer!!, 4 * mTexCoordBuffer!!.capacity())
            gl11.glTexCoordPointer(2, GL10.GL_FIXED, 0, 0)
        } else {
            gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, mTexCoordBuffer)
        }
    }

    private fun regenerateBuffer() {
        if (mNumVertices == 0) {
            return
        }
        val bb = ByteBuffer.allocateDirect(4 * 2 * mNumVertices)
        bb.order(ByteOrder.nativeOrder())
        val ib = bb.asIntBuffer()
        ib.position(0)
        mTexCoordBuffer = ib
    }

    private var mTexCoordBuffer: IntBuffer? = null
    private var mNumVertices = 0
    private val mGLBuffer = GLBuffer(GL11.GL_ARRAY_BUFFER)
    private var mUseVBO = false
}