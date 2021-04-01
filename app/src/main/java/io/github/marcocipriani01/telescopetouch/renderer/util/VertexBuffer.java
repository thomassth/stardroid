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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import io.github.marcocipriani01.telescopetouch.maths.MathsUtils;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

public class VertexBuffer {

    private final GLBuffer mGLBuffer = new GLBuffer(GL11.GL_ARRAY_BUFFER);
    private final boolean mUseVBO;
    private IntBuffer mPositionBuffer = null;
    private int mNumVertices = 0;

    public VertexBuffer(boolean useVBO) {
        mNumVertices = 0;
        mUseVBO = useVBO;
    }

    public VertexBuffer(int numVertices) {
        this(numVertices, false);
    }

    public VertexBuffer(int numVertices, boolean useVBO) {
        mUseVBO = useVBO;
        reset(numVertices);
    }

    public int size() {
        return mNumVertices;
    }

    public void reset(int numVertices) {
        mNumVertices = numVertices;
        regenerateBuffer();
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    public void reload() {
        mGLBuffer.reload();
    }

    public void addPoint(Vector3 p) {
        addPoint((float) p.x, (float) p.y, (float) p.z);
    }

    public void addPoint(float x, float y, float z) {
        mPositionBuffer.put(MathsUtils.floatToFixedPoint(x));
        mPositionBuffer.put(MathsUtils.floatToFixedPoint(y));
        mPositionBuffer.put(MathsUtils.floatToFixedPoint(z));
    }

    public void set(GL10 gl) {
        if (mNumVertices == 0) {
            return;
        }

        mPositionBuffer.position(0);

        if (mUseVBO && GLBuffer.canUseVBO()) {
            GL11 gl11 = (GL11) gl;
            mGLBuffer.bind(gl11, mPositionBuffer, 4 * mPositionBuffer.capacity());
            gl11.glVertexPointer(3, GL10.GL_FIXED, 0, 0);
        } else {
            gl.glVertexPointer(3, GL10.GL_FIXED, 0, mPositionBuffer);
        }
    }

    private void regenerateBuffer() {
        if (mNumVertices == 0) {
            return;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * 3 * mNumVertices);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();
        ib.position(0);
        mPositionBuffer = ib;
    }
}