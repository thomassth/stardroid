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
import android.content.SharedPreferences;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.HorizontalCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.TimeUtils;

public class CatalogArrayAdapter extends RecyclerView.Adapter<CatalogArrayAdapter.CatalogEntryHolder>
        implements SharedPreferences.OnSharedPreferenceChangeListener, SectionIndexer {

    private final List<CatalogEntry> entries;
    private final List<CatalogEntry> shownEntries = new ArrayList<>();
    private final Context context;
    private final LayoutInflater inflater;
    private final SharedPreferences preferences;
    private final ArrayList<Integer> sectionPositions = new ArrayList<>();
    private boolean showStars = true;
    private boolean showDso = true;
    private boolean showPlanets = true;
    private CatalogItemListener listener;
    private double limitMagnitude;
    private boolean onlyAboveHorizon = false;
    private Location location = null;

    public CatalogArrayAdapter(Context context, Catalog catalog) {
        super();
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        limitMagnitude = Double.parseDouble(preferences.getString(ApplicationConstants.CATALOG_LIMIT_MAGNITUDE, "5"));
        preferences.registerOnSharedPreferenceChangeListener(this);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.entries = catalog.getEntries();
        if (catalog.isReady()) shownEntries.addAll(entries);
    }

    public void detachPref() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean isShowStars() {
        return showStars;
    }

    public void starsShown(boolean showStars) {
        this.showStars = showStars;
        reloadCatalog();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isShowDso() {
        return showDso;
    }

    public void dsoShown(boolean showDso) {
        this.showDso = showDso;
        reloadCatalog();
    }

    public boolean planetsShown() {
        return showPlanets;
    }

    public void setShowPlanets(boolean showPlanets) {
        this.showPlanets = showPlanets;
        reloadCatalog();
    }

    public boolean isOnlyAboveHorizon() {
        return onlyAboveHorizon;
    }

    public void setOnlyAboveHorizon(boolean onlyAboveHorizon) {
        this.onlyAboveHorizon = onlyAboveHorizon;
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
        if (onlyAboveHorizon && (location != null)) {
            double latitude = location.getLatitude(),
                    siderealTime = TimeUtils.meanSiderealTime(Calendar.getInstance(), location.getLongitude());
            for (CatalogEntry entry : entries) {
                if (isVisible(entry) && HorizontalCoordinates.isObjectAboveHorizon(entry.coord, latitude, siderealTime)) {
                    shownEntries.add(entry);
                }
            }
        } else {
            for (CatalogEntry entry : entries) {
                if (isVisible(entry)) {
                    shownEntries.add(entry);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String string) {
        shownEntries.clear();
        if (onlyAboveHorizon && (location != null)) {
            double latitude = location.getLatitude(),
                    siderealTime = TimeUtils.meanSiderealTime(Calendar.getInstance(), location.getLongitude());
            for (CatalogEntry entry : entries) {
                if (entry.magnitudeDouble <= limitMagnitude) {
                    if (showStars && (entry instanceof StarEntry)) {
                        if (matches(((StarEntry) entry).getNames(), string.replace("HD ", "HD").replace("SAO ", "SAO"))
                                && HorizontalCoordinates.isObjectAboveHorizon(entry.coord, latitude, siderealTime))
                            shownEntries.add(entry);
                    } else if (((showDso && (entry instanceof DSOEntry)) ||
                            (showPlanets && (entry instanceof PlanetEntry))) && matches(entry.getName(), string)
                            && HorizontalCoordinates.isObjectAboveHorizon(entry.coord, latitude, siderealTime)) {
                        shownEntries.add(entry);
                    }
                }
            }
        } else {
            for (CatalogEntry entry : entries) {
                if (entry.magnitudeDouble <= limitMagnitude) {
                    if (showStars && (entry instanceof StarEntry)) {
                        if (matches(((StarEntry) entry).getNames(), string.replace("hd ", "hd").replace("sao ", "sao")))
                            shownEntries.add(entry);
                    } else if (((showDso && (entry instanceof DSOEntry)) ||
                            (showPlanets && (entry instanceof PlanetEntry))) && matches(entry.getName(), string)) {
                        shownEntries.add(entry);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matches(String a, String b) {
        return a.toLowerCase().contains(b);
    }

    private boolean isVisible(CatalogEntry entry) {
        return (entry.magnitudeDouble <= limitMagnitude) &&
                ((showStars && (entry instanceof StarEntry)) ||
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(ApplicationConstants.CATALOG_LIMIT_MAGNITUDE)) {
            try {
                limitMagnitude = Double.parseDouble(preferences.getString(key, "8"));
                reloadCatalog();
            } catch (NumberFormatException ignored) {

            }
        }
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>();
        sectionPositions.clear();
        for (int i = 0, size = shownEntries.size(); i < size; i++) {
            char c = shownEntries.get(i).name.toUpperCase().charAt(0);
            if (Character.isDigit(c)) c = '#';
            String s = String.valueOf(c);
            if (!sections.contains(s)) {
                sections.add(s);
                sectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return (sectionIndex < sectionPositions.size()) ? sectionPositions.get(sectionIndex) : 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
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