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

/**
 * A simple class for rendering a textured quad.
 *
 * @author James Powell
 */
class TexturedQuad(
    tex: TextureReference?,
    px: Float, py: Float, pz: Float,
    ux: Float, uy: Float, uz: Float,
    vx: Float, vy: Float, vz: Float
) {
    private var mTexCoords: TexCoordBuffer? = null
    private var mPosition: VertexBuffer? = null
    private var mTexture: TextureReference? = null
    fun draw(gl: GL10) {
        gl.glEnable(GL10.GL_TEXTURE_2D)
        mTexture!!.bind(gl)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY)
        mPosition!!.set(gl)
        mTexCoords!!.set(gl)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
        gl.glDisable(GL10.GL_TEXTURE_2D)
    }

    /**
     * Constructs the textured quad.
     * p is the point at the center of the quad.
     * u is the vector from the center of quad, pointing right.
     * v is the vector from the center of the quad, pointing up.
     * The four vertices of the quad are: by p +/- u +/- v
     * @param tex The texture to apply to the quad
     * @param px
     * @param py
     * @param pz
     * @param ux
     * @param uy
     * @param uz
     * @param vx
     * @param vy
     * @param vz
     */
    init {
        mPosition = VertexBuffer(12)
        mTexCoords = TexCoordBuffer(12)
        val vertexBuffer: VertexBuffer = mPosition as VertexBuffer
        val texCoordBuffer: TexCoordBuffer = mTexCoords as TexCoordBuffer

        // Upper left
        vertexBuffer.addPoint(px - ux - vx, py - uy - vy, pz - uz - vz)
        texCoordBuffer.addTexCoords(0f, 1f)

        // upper left
        vertexBuffer.addPoint(px - ux + vx, py - uy + vy, pz - uz + vz)
        texCoordBuffer.addTexCoords(0f, 0f)

        // lower right
        vertexBuffer.addPoint(px + ux - vx, py + uy - vy, pz + uz - vz)
        texCoordBuffer.addTexCoords(1f, 1f)

        // upper right
        vertexBuffer.addPoint(px + ux + vx, py + uy + vy, pz + uz + vz)
        texCoordBuffer.addTexCoords(1f, 0f)
        mTexture = tex
    }
}