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

package io.github.marcocipriani01.telescopetouch.layers;

import java.util.List;
import java.util.Set;

import io.github.marcocipriani01.telescopetouch.renderer.RendererController;
import io.github.marcocipriani01.telescopetouch.search.SearchResult;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;


/**
 * A logical collection of objects which should be displayed in SkyMap. For
 * instance, the set of objects which should be turned off / on simultaneously.
 *
 * @author Brent Bryan
 */
public interface Layer {

    /**
     * Initializes the layer; reading data and computing locations as necessary.
     * This method should return quickly - use a background thread if necessary.
     * This method is typically called before the {@link #registerWithRenderer}
     * method, but may not be.
     */
    void initialize();

    /**
     * Registers this layer with the given {@link RendererController}.  None of
     * the objects in this layer can be displayed until this method is called.
     */
    void registerWithRenderer(RendererController controller);

    /**
     * Returns the z-ordering of the layers.  Lower numbers are rendered first and
     * are therefore 'behind' higher numbered layers.
     */
    int getLayerDepthOrder();

    /**
     * Returns the preference label associated with this layer.
     */
    String getPreferenceId();

    /**
     * Returns the name associated with this layer.
     */
    String getLayerName();

    /**
     * Sets whether the {@link AstronomicalSource}s in this layer should be shown
     * by the renderer.
     */
    void setVisible(boolean visible);

    /**
     * Search the layer for an object with the given name.  The search is
     * case-insensitive.
     *
     * @param name the name to search for
     * @return a list of all matching objects.
     */
    List<SearchResult> searchByObjectName(String name);

    /**
     * Given a string prefix, find all possible queries for which we have a
     * search result.  The search is case-insensitive.
     *
     * @param prefix the prefix to search for.
     * @return a set of matching queries.
     */
    Set<String> getObjectNamesMatchingPrefix(String prefix);
}