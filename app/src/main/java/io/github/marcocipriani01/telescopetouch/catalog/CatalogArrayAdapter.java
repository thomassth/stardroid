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

package io.github.marcocipriani01.telescopetouch.catalog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;

public class CatalogArrayAdapter extends RecyclerView.Adapter<CatalogArrayAdapter.CatalogEntryHolder> {

    private final List<CatalogEntry> entries;
    private final List<CatalogEntry> shownEntries = new ArrayList<>();
    private final Context context;
    private final LayoutInflater inflater;
    private boolean showStars = true;
    private boolean showDso = true;
    private boolean showPlanets = true;
    private CatalogItemListener listener;

    public CatalogArrayAdapter(Context context, Catalog catalog) {
        super();
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.entries = catalog.getEntries();
        if (catalog.isReady()) shownEntries.addAll(entries);
    }

    public boolean isShowStars() {
        return showStars;
    }

    public void setShowStars(boolean showStars) {
        this.showStars = showStars;
        reloadCatalog();
    }

    public boolean isShowDso() {
        return showDso;
    }

    public void setShowDso(boolean showDso) {
        this.showDso = showDso;
        reloadCatalog();
    }

    public boolean isShowPlanets() {
        return showPlanets;
    }

    public void setShowPlanets(boolean showPlanets) {
        this.showPlanets = showPlanets;
        reloadCatalog();
    }

    public void setCatalogItemListener(CatalogItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CatalogEntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CatalogEntryHolder(inflater.inflate(R.layout.database_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogEntryHolder holder, int position) {
        CatalogEntry entry = shownEntries.get(position);
        holder.text1.setText(entry.getName());
        holder.text2.setText(entry.createSummary(context));
    }

    @Override
    public int getItemCount() {
        return shownEntries.size();
    }

    public void reloadCatalog() {
        shownEntries.clear();
        for (CatalogEntry entry : entries) {
            if (isVisible(entry)) {
                shownEntries.add(entry);
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String string) {
        shownEntries.clear();
        for (CatalogEntry entry : entries) {
            if (isVisible(entry) && entry.getName().toLowerCase().contains(string.toLowerCase())) {
                shownEntries.add(entry);
            }
        }
        notifyDataSetChanged();
    }

    private boolean isVisible(CatalogEntry entry) {
        return ((showStars && (entry instanceof StarEntry)) ||
                (showDso && (entry instanceof DSOEntry)) ||
                (showPlanets && (entry instanceof PlanetEntry)));
    }

    public int visibleItemsCount() {
        return shownEntries.size();
    }

    public CatalogEntry getEntryAt(int position) {
        return shownEntries.get(position);
    }

    public boolean isEmpty() {
        return shownEntries.isEmpty();
    }

    public interface CatalogItemListener {
        void onCatalogItemClick(View v);
    }

    public class CatalogEntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text1, text2;

        public CatalogEntryHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) listener.onCatalogItemClick(v);
        }
    }
}