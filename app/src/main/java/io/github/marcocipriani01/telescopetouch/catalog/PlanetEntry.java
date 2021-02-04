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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import java.util.Calendar;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.Planet;

public class PlanetEntry extends CatalogEntry {

    @SuppressLint("DefaultLocale")
    private PlanetEntry(Planet planet, String name, Calendar time, HeliocentricCoordinates sun) {
        this.name = name;
        coord = planet.getEquatorialCoordinates(time, sun);
        magnitudeDouble = planet.getMagnitude(time);
        this.magnitude = String.format("%.2f", magnitudeDouble);
    }

    public static void loadToList(List<CatalogEntry> list, Resources resources) {
        Calendar time = Calendar.getInstance();
        HeliocentricCoordinates sun = HeliocentricCoordinates.getInstance(Planet.Sun, time);
        for (Planet planet : Planet.values()) {
            list.add(new PlanetEntry(planet, planet.getName(resources), time, sun));
        }
    }

    /**
     * Create the description rich-text string
     *
     * @param context Context (to access resource strings)
     * @return description Spannable
     */
    @Override
    public Spannable createDescription(Context context, Location location) {
        Resources r = context.getResources();
        return new SpannableString(Html.fromHtml("<b>" +
                r.getString(R.string.planet) + ": </b>" + name + "<br><b>" +
                r.getString(R.string.entry_magnitude) + ": </b>" + magnitude + "<br>" +
                getCoordinatesString(r, location)));
    }

    /**
     * Create the summary rich-text string (1 line)
     *
     * @param context Context (to access resource strings)
     * @return summary Spannable
     */
    @Override
    public Spannable createSummary(Context context) {
        return new SpannableString(Html.fromHtml("<b>" + context.getString(R.string.solar_system) + "</b>"));
    }

    @Override
    public int getIconResource() {
        return R.drawable.planets_on;
    }
}