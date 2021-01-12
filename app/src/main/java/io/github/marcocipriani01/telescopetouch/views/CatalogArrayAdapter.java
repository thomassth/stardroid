/*
 * Copyright (C) 2020  Marco Cipriani (@marcocipriani01)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.github.marcocipriani01.telescopetouch.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.catalog.Catalog;
import io.github.marcocipriani01.telescopetouch.catalog.CatalogEntry;
import io.github.marcocipriani01.telescopetouch.catalog.DSOEntry;
import io.github.marcocipriani01.telescopetouch.catalog.PlanetEntry;
import io.github.marcocipriani01.telescopetouch.catalog.StarEntry;

public class CatalogArrayAdapter extends ArrayAdapter<CatalogEntry> {

    private final List<CatalogEntry> entries;
    private boolean showStars = true;
    private boolean showDso = true;
    private boolean showPlanets = true;

    public CatalogArrayAdapter(@NonNull Context context, Catalog catalog) {
        super(context, android.R.layout.simple_list_item_2, android.R.id.text1);
        this.entries = catalog.getEntries();
        if (entries.size() > 0) addAll(entries);
    }

    public void setShowStars(boolean showStars) {
        this.showStars = showStars;
        reloadCatalog();
    }

    public void setShowDso(boolean showDso) {
        this.showDso = showDso;
        reloadCatalog();
    }

    public void setShowPlanets(boolean showPlanets) {
        this.showPlanets = showPlanets;
        reloadCatalog();
    }

    public void reloadCatalog() {
        setNotifyOnChange(false);
        clear();
        for (CatalogEntry entry : entries) {
            if (isVisible(entry)) {
                setNotifyOnChange(false);
                add(entry);
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String string) {
        setNotifyOnChange(false);
        clear();
        for (CatalogEntry entry : entries) {
            if (isVisible(entry) && entry.getName().toLowerCase().contains(string.toLowerCase())) {
                setNotifyOnChange(false);
                add(entry);
            }
        }
        notifyDataSetChanged();
    }

    private boolean isVisible(CatalogEntry entry) {
        return ((showStars && (entry instanceof StarEntry)) ||
                (showDso && (entry instanceof DSOEntry)) ||
                (showPlanets && (entry instanceof PlanetEntry)));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        CatalogEntry item = getItem(position);
        ((TextView) view.findViewById(android.R.id.text1)).setText(item.getName());
        ((TextView) view.findViewById(android.R.id.text2)).setText(item.createSummary(getContext()));
        return view;
    }
}