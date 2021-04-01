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

public class ColorBuffer {

    private final GLBuffer mGLBuffer = new GLBuffer(GL11.GL_ARRAY_BUFFER);
    private IntBuffer mColorBuffer = null;
    private int mNumVertices;
    private boolean mUseVBO;

    public ColorBuffer(int numVertices) {
        reset(numVertices);
    }

    public ColorBuffer() {
        mNumVertices = 0;
    }

    public ColorBuffer(boolean useVBO) {
        mNumVertices = 0;
        mUseVBO = useVBO;
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

    public void addColor(int a, int r, int g, int b) {
        addColor(((a & 0xff) << 24) | ((b & 0xff) << 16) | ((g & 0xff) << 8) | (r & 0xff));
    }

    public void addColor(int abgr) {
        mColorBuffer.put(abgr);
    }

    public void set(GL10 gl) {
        if (mNumVertices == 0) {
            return;
        }
        mColorBuffer.position(0);

        if (mUseVBO && GLBuffer.canUseVBO()) {
            GL11 gl11 = (GL11) gl;
            mGLBuffer.bind(gl11, mColorBuffer, 4 * mColorBuffer.capacity());
            gl11.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, 0);
        } else {
            gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, mColorBuffer);
        }
    }

    private void regenerateBuffer() {
        if (mNumVertices == 0) {
            return;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * mNumVertices);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();
        ib.position(0);
        mColorBuffer = ib;
    }
}