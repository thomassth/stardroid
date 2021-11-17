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
import com.google.android.stardroid.renderer.util.GLBuffer.Companion.unbind
import kotlin.jvm.JvmOverloads
import javax.microedition.khronos.opengles.GL10
import com.google.android.stardroid.renderer.util.GLBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL11

class IndexBuffer {
    constructor() {
        mNumIndices = 0
    }

    constructor(useVBO: Boolean) {
        mNumIndices = 0
        mUseVbo = useVBO
    }

    @JvmOverloads
    constructor(numVertices: Int, useVbo: Boolean = false) {
        mUseVbo = useVbo
        reset(numVertices)
    }

    fun size(): Int {
        return mNumIndices
    }

    fun reset(numVertices: Int) {
        mNumIndices = numVertices
        regenerateBuffer()
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    fun reload() {
        mGLBuffer.reload()
    }

    private fun regenerateBuffer() {
        if (mNumIndices == 0) {
            return
        }
        val bb = ByteBuffer.allocateDirect(2 * mNumIndices)
        bb.order(ByteOrder.nativeOrder())
        val ib = bb.asShortBuffer()
        ib.position(0)
        mIndexBuffer = ib
    }

    fun addIndex(index: Short) {
        mIndexBuffer!!.put(index)
    }

    fun draw(gl: GL10, primitiveType: Int) {
        if (mNumIndices == 0) {
            return
        }
        mIndexBuffer!!.position(0)
        if (mUseVbo && canUseVBO()) {
            val gl11 = gl as GL11
            mGLBuffer.bind(gl11, mIndexBuffer!!, 2 * mIndexBuffer!!.capacity())
            gl11.glDrawElements(primitiveType, size(), GL10.GL_UNSIGNED_SHORT, 0)
            unbind(gl11)
        } else {
            gl.glDrawElements(primitiveType, size(), GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
        }
    }

    private var mIndexBuffer: ShortBuffer? = null
    private var mNumIndices = 0
    private val mGLBuffer = GLBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER)
    private var mUseVbo = false
}