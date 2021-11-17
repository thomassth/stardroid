// Copyright 2009 Google Inc.
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
package com.google.android.stardroid.renderer

import android.content.res.Resources
import com.google.android.stardroid.R
import com.google.android.stardroid.math.MathUtils.sin
import com.google.android.stardroid.math.TWO_PI
import com.google.android.stardroid.renderer.util.SearchHelper
import com.google.android.stardroid.renderer.util.TextureManager
import com.google.android.stardroid.renderer.util.TextureReference
import com.google.android.stardroid.renderer.util.TexturedQuad
import javax.microedition.khronos.opengles.GL10

class CrosshairOverlay {
    fun reloadTextures(gl: GL10, res: Resources?, textureManager: TextureManager) {
        // Load the crosshair texture.
        mTex = textureManager.getTextureFromResource(gl, R.drawable.crosshair)
    }

    fun resize(gl: GL10?, screenWidth: Int, screenHeight: Int) {
        mQuad = TexturedQuad(
            mTex,
            0F, 0F, 0F,
            40.0f / screenWidth, 0F, 0F,
            0F, 40.0f / screenHeight, 0F
        )
    }

    fun draw(gl: GL10, searchHelper: SearchHelper, nightVisionMode: Boolean) {
        // Return if the label has a negative z.
        val (x, y, z) = searchHelper.transformedPosition
        if (z < 0) {
            return
        }
        gl.glPushMatrix()
        gl.glLoadIdentity()
        gl.glTranslatef(x, y, 0f)
        val period = 1000
        val time = System.currentTimeMillis()
        val intensity = 0.7f + 0.3f * sin(time % period * TWO_PI / period)
        if (nightVisionMode) {
            gl.glColor4f(intensity, 0f, 0f, 0.7f)
        } else {
            gl.glColor4f(intensity, intensity, 0f, 0.7f)
        }
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
        mQuad!!.draw(gl)
        gl.glDisable(GL10.GL_BLEND)
        gl.glPopMatrix()
    }

    private var mQuad: TexturedQuad? = null
    private var mTex: TextureReference? = null
}