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

public class ColoredQuad {
    private final float mR;
    private final float mG;
    private final float mB;
    private final float mA;
    private final VertexBuffer mPosition;

    public ColoredQuad(float r, float g, float b, float a,
                       float px, float py, float pz,
                       float ux, float uy, float uz,
                       float vx, float vy, float vz) {
        mPosition = new VertexBuffer(12);
        VertexBuffer vertexBuffer = mPosition;

        // Upper left
        vertexBuffer.addPoint(px - ux - vx, py - uy - vy, pz - uz - vz);

        // upper left
        vertexBuffer.addPoint(px - ux + vx, py - uy + vy, pz - uz + vz);

        // lower right
        vertexBuffer.addPoint(px + ux - vx, py + uy - vy, pz + uz - vz);

        // upper right
        vertexBuffer.addPoint(px + ux + vx, py + uy + vy, pz + uz + vz);

        mR = r;
        mG = g;
        mB = b;
        mA = a;
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        // Enable blending if alpha != 1.
        if (mA != 1) {
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glDisable(GL10.GL_TEXTURE_2D);

        mPosition.set(gl);
        gl.glColor4f(mR, mG, mB, mA);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

        gl.glEnable(GL10.GL_TEXTURE_2D);

        // Disable blending if alpha != 1.
        if (mA != 1) {
            gl.glDisable(GL10.GL_BLEND);
        }
    }
}