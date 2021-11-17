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

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.microedition.khronos.opengles.GL10

/**
 * Manages all textures used by the application.  Useful to make sure that we don't accidentally
 * use deleted textures and don't leak textures, and that we don't create multiple instances of the
 * same texture.
 *
 * TODO(jpowell): We only ever need one instance of this class, but it would be cleaner if it was
 * a normal class instead of a global singleton, so I should change it when I get a chance.
 *
 * @author James Powell
 */
class TextureManager(private val mRes: Resources) {
    private val mResourceIdToTextureMap: MutableMap<Int, TextureData> = HashMap()
    private val mAllTextures = ArrayList<TextureReferenceImpl>()
    fun createTexture(gl: GL10): TextureReference {
        return createTextureInternal(gl)
    }

    fun getTextureFromResource(gl: GL10, resourceID: Int): TextureReference? {
        // If the texture already exists, return it.
        val texData = mResourceIdToTextureMap[resourceID]
        if (texData != null) {
            // Increment the reference count
            texData.refCount++
            return texData.ref
        }
        val tex = createTextureFromResource(gl, resourceID)

        // Add it to the map.
        val data = TextureData()
        data.ref = tex
        data.refCount = 1
        mResourceIdToTextureMap[resourceID] = data
        return tex
    }

    fun reset() {
        mResourceIdToTextureMap.clear()
        for (ref in mAllTextures) {
            ref.invalidate()
        }
        mAllTextures.clear()
    }

    private class TextureReferenceImpl(val iD: Int) : TextureReference {
        override fun bind(gl: GL10?) {
            checkValid()
            gl!!.glBindTexture(GL10.GL_TEXTURE_2D, iD)
        }

        override fun delete(gl: GL10?) {
            checkValid()
            gl!!.glDeleteTextures(1, intArrayOf(iD), 0)
            invalidate()
        }

        fun invalidate() {
            mValid = false
        }

        private fun checkValid() {
            if (!mValid) {
                Log.e("TextureManager", "Setting invalidated texture ID: " + iD)
                val writer = StringWriter()
                Throwable().printStackTrace(PrintWriter(writer))
                Log.e("TextureManager", writer.toString())
            }
        }

        private var mValid = true
    }

    private class TextureData {
        var ref: TextureReferenceImpl? = null
        var refCount = 0
    }

    private fun createTextureFromResource(gl: GL10, resourceID: Int): TextureReferenceImpl {
        // The texture hasn't been loaded yet, so load it.
        val tex = createTextureInternal(gl)
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        val bmp = BitmapFactory.decodeResource(mRes, resourceID, opts)
        tex.bind(gl)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0)
        bmp.recycle()
        return tex
    }

    private fun createTextureInternal(gl: GL10): TextureReferenceImpl {
        // The texture hasn't been loaded yet, so load it.
        val texID = IntArray(1)
        gl.glGenTextures(1, texID, 0)
        val tex = TextureReferenceImpl(texID[0])
        mAllTextures.add(tex)
        return tex
    }
}