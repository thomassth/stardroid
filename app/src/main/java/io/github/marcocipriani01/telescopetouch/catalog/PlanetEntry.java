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
import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.Planet;

public class PlanetEntry extends CatalogEntry {

    private final Planet planet;

    @SuppressLint("DefaultLocale")
    private PlanetEntry(Planet planet, Resources resources, Calendar time, HeliocentricCoordinates sun) {
        this.planet = planet;
        this.name = planet.getName(resources);
        coord = planet.getEquatorialCoordinates(time, sun);
        magnitudeDouble = planet.getMagnitude(time);
        this.magnitude = String.format("%.2f", magnitudeDouble);
    }

    public static void loadToList(List<CatalogEntry> list, Resources resources) {
        Calendar time = Calendar.getInstance();
        HeliocentricCoordinates sun = HeliocentricCoordinates.getInstance(Planet.Sun, time);
        for (Planet planet : Planet.values()) {
            list.add(new PlanetEntry(planet, resources, time, sun));
        }
    }

    public Planet getPlanet() {
        return planet;
    }

    public int getGalleryResourceId() {
        return (planet == Planet.Moon) ? planet.getMapResourceId(Calendar.getInstance()) : planet.getGalleryResourceId();
    }

    @Override
    public EquatorialCoordinates getCoordinates() {
        Calendar time = Calendar.getInstance();
        HeliocentricCoordinates sun = HeliocentricCoordinates.getInstance(Planet.Sun, time);
        coord = planet.getEquatorialCoordinates(time, sun);
        return super.getCoordinates();
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