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

import com.google.android.stardroid.util.FixedPoint.floatToFixedPoint
import com.google.android.stardroid.renderer.util.GLBuffer.Companion.canUseVBO
import kotlin.jvm.JvmOverloads
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.util.FixedPoint
import javax.microedition.khronos.opengles.GL10
import com.google.android.stardroid.renderer.util.GLBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL11

class VertexBuffer {
    //Creates an empty buffer.  Must call reset() before adding vertices.
    constructor() {
        mNumVertices = 0
        mUseVBO = false
    }

    constructor(useVBO: Boolean) {
        mNumVertices = 0
        mUseVBO = useVBO
    }

    @JvmOverloads
    constructor(numVertices: Int, useVBO: Boolean = false) {
        mUseVBO = useVBO
        reset(numVertices)
    }

    fun size(): Int {
        return mNumVertices
    }

    fun reset(numVertices: Int) {
        mNumVertices = numVertices
        regenerateBuffer()
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    fun reload() {
        mGLBuffer.reload()
    }

    fun addPoint(p: Vector3) {
        addPoint(p.x, p.y, p.z)
    }

    fun addPoint(x: Float, y: Float, z: Float) {
        mPositionBuffer!!.put(floatToFixedPoint(x))
        mPositionBuffer!!.put(floatToFixedPoint(y))
        mPositionBuffer!!.put(floatToFixedPoint(z))
    }

    fun set(gl: GL10) {
        if (mNumVertices == 0) {
            return
        }
        mPositionBuffer!!.position(0)
        if (mUseVBO && canUseVBO()) {
            val gl11 = gl as GL11
            mGLBuffer.bind(gl11, mPositionBuffer!!, 4 * mPositionBuffer!!.capacity())
            gl11.glVertexPointer(3, GL10.GL_FIXED, 0, 0)
        } else {
            gl.glVertexPointer(3, GL10.GL_FIXED, 0, mPositionBuffer)
        }
    }

    private fun regenerateBuffer() {
        if (mNumVertices == 0) {
            return
        }
        val bb = ByteBuffer.allocateDirect(4 * 3 * mNumVertices)
        bb.order(ByteOrder.nativeOrder())
        val ib = bb.asIntBuffer()
        ib.position(0)
        mPositionBuffer = ib
    }

    private var mPositionBuffer: IntBuffer? = null
    private var mNumVertices = 0
    private val mGLBuffer = GLBuffer(GL11.GL_ARRAY_BUFFER)
    private var mUseVBO = false
}