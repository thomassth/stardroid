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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.renderer.RendererController;
import io.github.marcocipriani01.telescopetouch.search.SearchResult;
import io.github.marcocipriani01.telescopetouch.search.SearchTermsProvider.SearchTerm;

/**
 * Allows a group of layers to be controlled together.
 */
public class LayerManager implements OnSharedPreferenceChangeListener {

    private static final String TAG = TelescopeTouchApp.getTag(LayerManager.class);
    private final List<Layer> layers = new ArrayList<>();
    private final SharedPreferences preferences;

    public LayerManager(SharedPreferences preferences) {
        Log.d(TAG, "Creating LayerManager");
        this.preferences = preferences;
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void addLayer(Layer layer) {
        this.layers.add(layer);
    }

    public void initialize() {
        for (Layer layer : layers) {
            layer.initialize();
        }
    }

    public void registerWithRenderer(RendererController renderer) {
        for (Layer layer : layers) {
            layer.registerWithRenderer(renderer);
            layer.setVisible(preferences.getBoolean(layer.getPreferenceId(), true));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        for (Layer layer : layers) {
            if (layer.getPreferenceId().equals(key)) {
                layer.setVisible(prefs.getBoolean(key, true));
                return;
            }
        }
    }

    /**
     * Search all visible layers for an object with the given name.
     *
     * @param name the name to search for
     * @return a list of all matching objects.
     */
    public List<SearchResult> searchByObjectName(String name) {
        List<SearchResult> all = new ArrayList<>();
        for (Layer layer : layers) {
            if (isLayerVisible(layer)) all.addAll(layer.searchByObjectName(name));
        }
        Log.d(TAG, "Got " + all.size() + " results in total for " + name);
        return all;
    }

    /**
     * Given a string prefix, find all possible queries for which we have a
     * result in the visible layers.
     *
     * @param prefix the prefix to search for.
     * @return a set of matching queries.
     */
    public Set<SearchTerm> getObjectNamesMatchingPrefix(String prefix) {
        Set<SearchTerm> all = new HashSet<>();
        for (Layer layer : layers) {
            if (isLayerVisible(layer)) {
                for (String query : layer.getObjectNamesMatchingPrefix(prefix)) {
                    SearchTerm result = new SearchTerm(query, layer.getLayerName());
                    all.add(result);
                }
            }
        }
        Log.d(TAG, "Got " + all.size() + " results in total for " + prefix);
        return all;
    }

    private boolean isLayerVisible(Layer layer) {
        return preferences.getBoolean(layer.getPreferenceId(), true);
    }
}