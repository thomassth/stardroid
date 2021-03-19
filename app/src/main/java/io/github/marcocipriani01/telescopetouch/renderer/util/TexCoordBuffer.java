/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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