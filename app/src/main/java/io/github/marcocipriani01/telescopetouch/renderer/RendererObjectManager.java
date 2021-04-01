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

import android.util.Log;

import java.util.EnumSet;

import javax.microedition.khronos.opengles.GL10;

import io.github.marcocipriani01.telescopetouch.renderer.util.TextureManager;


public abstract class RendererObjectManager implements Comparable<RendererObjectManager> {

    private static final float MAX_RADIUS_OF_VIEW = 360f;
    /**
     * Used to distinguish between different renderers, so we can have sets of them.
     */
    private static int sIndex = 0;
    private final TextureManager textureManager;
    private final int layer;
    private final int index;
    private boolean enabled = true;
    private SkyRenderer.RenderState renderState = null;
    private UpdateListener listener = null;

    public RendererObjectManager(int layer, TextureManager textureManager) {
        this.layer = layer;
        this.textureManager = textureManager;
        synchronized (RendererObjectManager.class) {
            index = sIndex++;
        }
    }

    public void enable(boolean enable) {
        enabled = enable;
    }

    public int compareTo(RendererObjectManager rom) {
        if (getClass() != rom.getClass()) {
            return getClass().getName().compareTo(rom.getClass().getName());
        }
        return Integer.compare(index, rom.index);
    }

    final int getLayer() {
        return layer;
    }

    final void draw(GL10 gl) {
        if (enabled && renderState.getRadiusOfView() <= MAX_RADIUS_OF_VIEW) {
            drawInternal(gl);
        }
    }

    final SkyRenderer.RenderState getRenderState() {
        return renderState;
    }

    final void setRenderState(SkyRenderer.RenderState state) {
        renderState = state;
    }

    final void setUpdateListener(UpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Notifies the renderer that the manager must be reloaded before the next time it is drawn.
     */
    final void queueForReload() {
        listener.queueForReload(this);
    }

    protected void logUpdateMismatch(String managerType, int expectedLength, int actualLength,
                                     EnumSet<RendererObjectManager.UpdateType> type) {
        Log.e("ImageObjectManager",
                "Trying to update objects in " + managerType + ", but number of input sources was "
                        + "different from the number currently set on the manager (" + actualLength
                        + " vs " + expectedLength + "\n"
                        + "Update options were: " + type + "\n"
                        + "Ignoring update");
    }

    protected TextureManager textureManager() {
        return textureManager;
    }

    /**
     * Reload all OpenGL resources needed by the object (ie, textures, VBOs).  If fullReload is true,
     * this means that the object needs to reload everything (this is the case when the object
     * is loaded for the first time, or when the activity is being recreated, and all the previous
     * resources have been invalid.  Sometimes a manager may only need to be partially reloaded (for
     * example, if new objects are set, they might need to be reloaded, but the texture shared
     * between them all is the same so it does not need to be).  The renderer will only ever do a
     * full reload - fullReload will only be false if the manager queues itself for a partial reload.
     */
    public abstract void reload(GL10 gl, boolean fullReload);

    protected abstract void drawInternal(GL10 gl);

    /**
     * Specifies options for updating a specific RendererObjectManager.
     */
    public enum UpdateType {
        Reset,            // Throw away any previous data and set entirely new data.
        UpdatePositions,  // Only update positions of existing objects.
        UpdateImages      // Only update images of existing objects.
    }

    interface UpdateListener {
        void queueForReload(RendererObjectManager rom);
    }
}