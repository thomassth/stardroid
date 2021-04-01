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
 * Represents a reference to an OpenGL texture.  You may bind the texture to
 * set it active, or delete it.  Normal Java garbage collection will not
 * reclaim this, so you must delete it when you are done with it.  Note that
 * when the OpenGL surface is re-created, any existing texture references are
 * automatically invalidated and should not be bound or deleted.
 *
 * @author James Powell
 */
public interface TextureReference {
    /**
     * Sets this as the active texture on the OpenGL context.
     *
     * @param gl The OpenGL context
     */
    void bind(GL10 gl);

    /**
     * Deletes the texture resource.  This should not be called multiple times.
     * Note that when the OpenGL surface is being re-created, all resources
     * are automatically freed, so you should not delete the textures in that
     * case.
     */
    void delete(GL10 gl);
}