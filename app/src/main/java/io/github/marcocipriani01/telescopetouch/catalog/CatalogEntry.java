/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
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

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.text.Spannable;

import androidx.annotation.NonNull;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.HorizontalCoordinates;

/**
 * An abstract astronomical object.
 */
public abstract class CatalogEntry implements Comparable<CatalogEntry> {

    /**
     * Coordinates.
     */
    protected EquatorialCoordinates coord;
    /**
     * Name.
     */
    protected String name;
    /**
     * Magnitude.
     */
    protected String magnitude;
    protected double magnitudeDouble = 0.0f;

    /**
     * @return the stored coordinates.
     */
    public EquatorialCoordinates getCoordinates() {
        return coord;
    }

    /**
     * @return the object's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Create the description rich-text string
     *
     * @param context Context (to access resource strings)
     * @return description Spannable
     */
    public abstract Spannable createDescription(Context context, Location location);

    /**
     * Create the summary rich-text string (1 line)
     *
     * @param context Context (to access resource strings)
     * @return summary Spannable
     */
    public abstract Spannable createSummary(Context context);

    public abstract int getIconResource();

    protected String getCoordinatesString(Resources r, Location location) {
        if (location == null) {
            return "<b>" + r.getString(R.string.entry_RA) + ": </b>" + coord.getRAString() + "<br><b>" +
                    r.getString(R.string.entry_Dec) + ": </b>" + coord.getDecString();
        } else {
            HorizontalCoordinates altAz = HorizontalCoordinates.getInstance(coord, location, Calendar.getInstance());
            return "<b>" + r.getString(R.string.entry_RA) + ": </b>" + coord.getRAString() + "<br><b>" +
                    r.getString(R.string.entry_Dec) + ": </b>" + coord.getDecString() + "<br><b>" +
                    r.getString(R.string.entry_alt) + ": </b>" + altAz.getAltString() + "<br><b>" +
                    r.getString(R.string.entry_az) + ": </b>" + altAz.getAzString() +
                    ((altAz.alt < 0) ? ("<br><br>" + r.getString(R.string.below_horizon_warning)) : "");
        }
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(@NonNull CatalogEntry another) {
        return this.getName().compareToIgnoreCase(another.getName());
    }
}