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

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import io.github.marcocipriani01.telescopetouch.maths.MathsUtils;

public class TexCoordBuffer {

    private final GLBuffer gLBuffer = new GLBuffer(GL11.GL_ARRAY_BUFFER);
    private IntBuffer texCoordBuffer = null;
    private int numVertices = 0;
    private boolean useVBO = false;

    public TexCoordBuffer(int numVertices) {
        reset(numVertices);
    }

    public TexCoordBuffer(boolean useVBO) {
        numVertices = 0;
        this.useVBO = useVBO;
    }

    public int size() {
        return numVertices;
    }

    public void reset(int numVertices) {
        if (numVertices < 0) {
            Log.e("TexCoordBuffer", "reset attempting to set numVertices to " + numVertices);
            numVertices = 0;
        }
        this.numVertices = numVertices;
        regenerateBuffer();
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    public void reload() {
        gLBuffer.reload();
    }

    public void addTexCoords(float u, float v) {
        texCoordBuffer.put(MathsUtils.floatToFixedPoint(u));
        texCoordBuffer.put(MathsUtils.floatToFixedPoint(v));
    }

    public void set(GL10 gl) {
        if (numVertices == 0) {
            return;
        }
        texCoordBuffer.position(0);

        if (useVBO && GLBuffer.canUseVBO()) {
            GL11 gl11 = (GL11) gl;
            gLBuffer.bind(gl11, texCoordBuffer, 4 * texCoordBuffer.capacity());
            gl11.glTexCoordPointer(2, GL10.GL_FIXED, 0, 0);
        } else {
            gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, texCoordBuffer);
        }
    }

    private void regenerateBuffer() {
        if (numVertices == 0) {
            return;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * 2 * numVertices);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();
        ib.position(0);
        texCoordBuffer = ib;
    }
}