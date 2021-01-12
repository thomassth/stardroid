package io.github.marcocipriani01.telescopetouch.catalog;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public void load() {
        if (ready || loading) throw new IllegalStateException("Catalog already loaded/loading!");
        Resources resources = TelescopeTouchApp.getAppResources();
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
            TelescopeTouchApp.log("Catalog loading error.");
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