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

import com.google.android.stardroid.renderer.util.GLBuffer.Companion.canUseVBO
import javax.microedition.khronos.opengles.GL10
import com.google.android.stardroid.renderer.util.GLBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL11

class ColorBuffer {
    constructor(numVertices: Int) {
        reset(numVertices)
    }

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
        mNumVertices = numVertices
        regenerateBuffer()
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    fun reload() {
        mGLBuffer.reload()
    }

    fun addColor(a: Int, r: Int, g: Int, b: Int) {
        addColor(a and 0xff shl 24 or (b and 0xff shl 16) or (g and 0xff shl 8) or (r and 0xff))
    }

    fun addColor(abgr: Int) {
        mColorBuffer!!.put(abgr)
    }

    fun set(gl: GL10) {
        if (mNumVertices == 0) {
            return
        }
        mColorBuffer!!.position(0)
        if (mUseVBO && canUseVBO()) {
            val gl11 = gl as GL11
            mGLBuffer.bind(gl11, mColorBuffer!!, 4 * mColorBuffer!!.capacity())
            gl11.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, 0)
        } else {
            gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, mColorBuffer)
        }
    }

    private fun regenerateBuffer() {
        if (mNumVertices == 0) {
            return
        }
        val bb = ByteBuffer.allocateDirect(4 * mNumVertices)
        bb.order(ByteOrder.nativeOrder())
        val ib = bb.asIntBuffer()
        ib.position(0)
        mColorBuffer = ib
    }

    private var mColorBuffer: IntBuffer? = null
    private var mNumVertices = 0
    private val mGLBuffer = GLBuffer(GL11.GL_ARRAY_BUFFER)
    private var mUseVBO = false
}