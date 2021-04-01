/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.renderer.util;

import javax.microedition.khronos.opengles.GL10;

/**
 * A simple class for rendering a textured quad.
 *
 * @author James Powell
 */
public class TexturedQuad {
    private final TexCoordBuffer mTexCoords;
    private final VertexBuffer mPosition;
    private final TextureReference mTexture;

    /**
     * Constructs the textured quad.
     * p is the point at the center of the quad.
     * u is the vector from the center of quad, pointing right.
     * v is the vector from the center of the quad, pointing up.
     * The four vertices of the quad are: by p +/- u +/- v
     *
     * @param tex The texture to apply to the quad
     */
    public TexturedQuad(TextureReference tex,
                        float px, float py, float pz,
                        float ux, float uy, float uz,
                        float vx, float vy, float vz) {
        mPosition = new VertexBuffer(12);
        mTexCoords = new TexCoordBuffer(12);

        VertexBuffer vertexBuffer = mPosition;
        TexCoordBuffer texCoordBuffer = mTexCoords;

        // Upper left
        vertexBuffer.addPoint(px - ux - vx, py - uy - vy, pz - uz - vz);
        texCoordBuffer.addTexCoords(0, 1);

        // upper left
        vertexBuffer.addPoint(px - ux + vx, py - uy + vy, pz - uz + vz);
        texCoordBuffer.addTexCoords(0, 0);

        // lower right
        vertexBuffer.addPoint(px + ux - vx, py + uy - vy, pz + uz - vz);
        texCoordBuffer.addTexCoords(1, 1);

        // upper right
        vertexBuffer.addPoint(px + ux + vx, py + uy + vy, pz + uz + vz);
        texCoordBuffer.addTexCoords(1, 0);

        mTexture = tex;
    }

    public void draw(GL10 gl) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        mTexture.bind(gl);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        mPosition.set(gl);
        mTexCoords.set(gl);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glDisable(GL10.GL_TEXTURE_2D);
    }
}