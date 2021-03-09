/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
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

package io.github.marcocipriani01.telescopetouch.catalog;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * A catalog of astronomical objects.
 *
 * @see DSOEntry
 * @see StarEntry
 */
public class Catalog {

    /**
     * Catalog objects.
     */
    private final List<CatalogEntry> entries = new ArrayList<>();
    private boolean ready = false;
    private boolean loading = false;
    private CatalogLoadingListener listener = null;

    public void load(Resources resources) {
        if (ready || loading) throw new IllegalStateException("Catalog already loaded/loading!");
        try {
            loading = true;
            Log.i("CatalogManager", "Loading planets...");
            PlanetEntry.loadToList(entries, resources);
            Log.i("CatalogManager", "Loading DSO...");
            DSOEntry.loadToList(entries, resources);
            Log.i("CatalogManager", "Loading stars...");
            StarEntry.loadToList(entries, resources);
            Collections.sort(entries);
            ready = true;
            callListener(true);
        } catch (Exception e) {
            Log.e("CatalogManager", "Unable to load catalog!", e);
            TelescopeTouchApp.connectionManager.log(resources.getString(R.string.catalog_load_error));
            callListener(false);
        }
    }

    private void callListener(boolean success) {
        loading = false;
        if (listener != null) {
            listener.onLoaded(success);
            listener = null;
        }
    }

    public void setListener(CatalogLoadingListener listener) {
        this.listener = listener;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * @return an {@link ArrayList} containing all the entries of this catalog.
     */
    public List<CatalogEntry> getEntries() {
        return entries;
    }

    public interface CatalogLoadingListener {
        void onLoaded(boolean success);
    }
}