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
import android.content.res.Resources;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import java.util.Calendar;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.Planet;
import io.github.marcocipriani01.telescopetouch.units.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.RaDec;

public class PlanetEntry extends CatalogEntry {

    private final Planet planet;

    private PlanetEntry(Planet planet, String name, Calendar time) {
        this.planet = planet;
        this.name = name;
        RaDec raDec = RaDec.getInstance(planet, time, HeliocentricCoordinates.getInstance(Planet.Sun, time));
        coord = new CatalogCoordinates(raDec.ra, raDec.dec);
    }

    public static void loadToList(List<CatalogEntry> list, Resources resources) {
        Calendar time = Calendar.getInstance();
        for (Planet planet : Planet.values()) {
            list.add(new PlanetEntry(planet, resources.getString(planet.getNameResourceId()), time));
        }
    }

    /**
     * Create the description rich-text string
     *
     * @param ctx Context (to access resource strings)
     * @return description Spannable
     */
    @Override
    public Spannable createDescription(Context ctx) {
        Resources r = ctx.getResources();
        String str = "<b>" + "Planet" + r.getString(R.string.colon_with_spaces) + "</b>" + r.getString(planet.getNameResourceId()) + "<br/>";
        str += "<b>" + r.getString(R.string.entry_RA) + r.getString(R.string.colon_with_spaces) + "</b>" + coord.getRaStr() + "<br/>";
        str += "<b>" + r.getString(R.string.entry_DE) + r.getString(R.string.colon_with_spaces) + "</b>" + coord.getDeStr();
        return new SpannableString(Html.fromHtml(str));
    }

    /**
     * Create the summary rich-text string (1 line)
     *
     * @param ctx Context (to access resource strings)
     * @return summary Spannable
     */
    @Override
    public Spannable createSummary(Context ctx) {
        Resources r = ctx.getResources();
        String str = "<b>" + "Solar system" + "</b>";
        return new SpannableString(Html.fromHtml(str));
    }
}