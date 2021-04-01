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

package io.github.marcocipriani01.telescopetouch.renderer;

import java.util.EnumSet;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;
import io.github.marcocipriani01.telescopetouch.renderer.util.IndexBuffer;
import io.github.marcocipriani01.telescopetouch.renderer.util.NightVisionColorBuffer;
import io.github.marcocipriani01.telescopetouch.renderer.util.TexCoordBuffer;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureManager;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureReference;
import io.github.marcocipriani01.telescopetouch.renderer.util.VertexBuffer;
import io.github.marcocipriani01.telescopetouch.source.LineSource;

public class PolyLineObjectManager extends RendererObjectManager {

    private final VertexBuffer mVertexBuffer = new VertexBuffer(true);
    private final NightVisionColorBuffer mColorBuffer = new NightVisionColorBuffer(true);
    private final TexCoordBuffer mTexCoordBuffer = new TexCoordBuffer(true);
    private final IndexBuffer mIndexBuffer = new IndexBuffer(true);
    private TextureReference mTexRef = null;
    private boolean mOpaque = true;

    public PolyLineObjectManager(int layer, TextureManager textureManager) {
        super(layer, textureManager);
    }

    public void updateObjects(List<LineSource> lines, EnumSet<UpdateType> updateType) {
        // We only care about updates to positions, ignore any other updates.
        if (!updateType.contains(UpdateType.Reset) &&
                !updateType.contains(UpdateType.UpdatePositions)) {
            return;
        }
        int numLineSegments = 0;
        for (LineSource l : lines) {
            numLineSegments += l.getVertices().size() - 1;
        }

        // To render everything in one call, we render everything as a line list
        // rather than a series of line strips.
        int numVertices = 4 * numLineSegments;
        int numIndices = 6 * numLineSegments;

        VertexBuffer vb = mVertexBuffer;
        vb.reset(4 * numLineSegments);
        NightVisionColorBuffer cb = mColorBuffer;
        cb.reset(4 * numLineSegments);
        TexCoordBuffer tb = mTexCoordBuffer;
        tb.reset(numVertices);
        IndexBuffer ib = mIndexBuffer;
        ib.reset(numIndices);

        // See comment in PointObjectManager for justification of this calculation.
        float fovyInRadians = 60 * (float) Math.PI / 180.0f;
        float sizeFactor = (float) Math.sin(fovyInRadians * 0.5f) / (float) Math.cos(fovyInRadians * 0.5f) / 480;

        boolean opaque = true;

        short vertexIndex = 0;
        for (LineSource l : lines) {
            List<GeocentricCoordinates> coords = l.getVertices();
            if (coords.size() < 2)
                continue;

            // If the color isn't fully opaque, set opaque to false.
            int color = l.getColor();
            opaque &= (color & 0xff000000) == 0xff000000;

            // Add the vertices.
            for (int i = 0; i < coords.size() - 1; i++) {
                Vector3 p1 = coords.get(i);
                Vector3 p2 = coords.get(i + 1);
                Vector3 u = Vector3.difference(p2, p1);
                // The normal to the quad should face the origin at its midpoint.
                Vector3 avg = Vector3.sum(p1, p2);
                avg.scale(0.5f);
                // I'm assuming that the points will already be on a unit sphere.  If this is not the case,
                // then we should normalize it here.
                Vector3 v = Vector3.normalized(Vector3.vectorProduct(u, avg));
                v.scale(sizeFactor * l.getLineWidth());


                // Add the vertices

                // Lower left corner
                vb.addPoint(Vector3.difference(p1, v));
                cb.addColor(color);
                tb.addTexCoords(0, 1);

                // Upper left corner
                vb.addPoint(Vector3.sum(p1, v));
                cb.addColor(color);
                tb.addTexCoords(0, 0);

                // Lower left corner
                vb.addPoint(Vector3.difference(p2, v));
                cb.addColor(color);
                tb.addTexCoords(1, 1);

                // Upper left corner
                vb.addPoint(Vector3.sum(p2, v));
                cb.addColor(color);
                tb.addTexCoords(1, 0);


                // Add the indices
                short bottomLeft = vertexIndex++;
                short topLeft = vertexIndex++;
                short bottomRight = vertexIndex++;
                short topRight = vertexIndex++;

                // First triangle
                ib.addIndex(bottomLeft);
                ib.addIndex(topLeft);
                ib.addIndex(bottomRight);

                // Second triangle
                ib.addIndex(bottomRight);
                ib.addIndex(topLeft);
                ib.addIndex(topRight);
            }
        }
        mOpaque = opaque;
    }

    @Override
    public void reload(GL10 gl, boolean fullReload) {
        mTexRef = textureManager().getTextureFromResource(gl, R.drawable.line);
        mVertexBuffer.reload();
        mColorBuffer.reload();
        mTexCoordBuffer.reload();
        mIndexBuffer.reload();
    }

    @Override
    protected void drawInternal(GL10 gl) {
        if (mIndexBuffer.size() == 0)
            return;

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        mTexRef.bind(gl);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CW);
        gl.glCullFace(GL10.GL_BACK);

        if (!mOpaque) {
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

        mVertexBuffer.set(gl);
        mColorBuffer.set(gl, getRenderState().getNightVisionMode());
        mTexCoordBuffer.set(gl);

        mIndexBuffer.draw(gl, GL10.GL_TRIANGLES);

        if (!mOpaque) {
            gl.glDisable(GL10.GL_BLEND);
        }

        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }
}