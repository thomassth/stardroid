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
import android.location.Location;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;

/**
 * Represents a star. This class also contains a loader to fetch stars from the app's catalog.
 */
public class StarEntry extends CatalogEntry {

    /**
     * Resource file.
     */
    private final static int RESOURCE = R.raw.stars;
    private final String names;

    /**
     * Create the entry from a catalog line
     */
    private StarEntry(String data) {
        String[] split = data.split("\\t");
        coord = new EquatorialCoordinates(Double.parseDouble(split[0].trim()), Double.parseDouble(split[1].trim()));
        magnitude = split[2].trim();
        if (!magnitude.equals(""))
            magnitudeDouble = Double.parseDouble(magnitude);
        String hd = "HD" + split[4].trim(),
                sao = "SAO" + split[3].trim(),
                con = (split.length > 6) ? split[5].trim().replace("  ", " ") : "";
        if (split.length == 7) {
            name = capitalize(split[6].trim().replace(";", ","));
            if (con.isEmpty()) {
                names = name + ", " + hd + ", " + sao;
            } else {
                names = name + ", " + con + ", " + hd + ", " + sao;
            }
        } else if (con.isEmpty()) {
            name = hd;
            names = hd + ", " + sao;
        } else {
            name = con;
            names = name + ", " + hd + ", " + sao;
        }
    }

    public static void loadToList(List<CatalogEntry> list, Resources resources) throws IOException {
        // Open and read the catalog file
        InputStream resourceStream = resources.openRawResource(RESOURCE);
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceStream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(new StarEntry(line));
        }
        resourceStream.close();
    }

    private static String capitalize(String string) {
        if (string.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        String[] split = string.split(" ");
        int i;
        for (i = 0; i < (split.length - 1); i++) {
            if (split[i].isEmpty()) continue;
            builder.append(split[i].charAt(0)).append(split[i].substring(1).toLowerCase()).append(" ");
        }
        builder.append(split[i].charAt(0)).append(split[i].substring(1).toLowerCase());
        return builder.toString();
    }

    public String getNames() {
        return names;
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
                r.getString(R.string.entry_names) + ": </b>" + names + "<br><b>" +
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
        Resources r = context.getResources();
        return new SpannableString(Html.fromHtml("<b>" +
                r.getString(R.string.entry_star) + "</b> " + r.getString(R.string.entry_mag) + ": " + magnitude));
    }

    @Override
    public int getIconResource() {
        return R.drawable.stars_on;
    }
}