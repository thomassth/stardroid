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
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class IndexBuffer {

    private final GLBuffer mGLBuffer = new GLBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER);
    private ShortBuffer mIndexBuffer = null;
    private int mNumIndices = 0;
    private boolean mUseVbo = false;

    public IndexBuffer(int numVertices) {
        this(numVertices, false);
    }

    public IndexBuffer() {
        mNumIndices = 0;
    }

    public IndexBuffer(boolean useVBO) {
        mNumIndices = 0;
        mUseVbo = useVBO;
    }

    public IndexBuffer(int numVertices, boolean useVbo) {
        mUseVbo = useVbo;
        reset(numVertices);
    }

    public int size() {
        return mNumIndices;
    }

    public void reset(int numVertices) {
        mNumIndices = numVertices;
        regenerateBuffer();
    }

    // Call this when we have to re-create the surface and reloading all OpenGL resources.
    public void reload() {
        mGLBuffer.reload();
    }

    private void regenerateBuffer() {
        if (mNumIndices == 0) {
            return;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(2 * mNumIndices);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer ib = bb.asShortBuffer();
        ib.position(0);
        mIndexBuffer = ib;
    }

    public void addIndex(short index) {
        mIndexBuffer.put(index);
    }

    public void draw(GL10 gl, int primitiveType) {
        if (mNumIndices == 0) {
            return;
        }
        mIndexBuffer.position(0);
        if (mUseVbo && GLBuffer.canUseVBO()) {
            GL11 gl11 = (GL11) gl;
            mGLBuffer.bind(gl11, mIndexBuffer, 2 * mIndexBuffer.capacity());
            gl11.glDrawElements(primitiveType, size(), GL10.GL_UNSIGNED_SHORT, 0);
            GLBuffer.unbind(gl11);
        } else {
            gl.glDrawElements(primitiveType, size(), GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        }
    }
}