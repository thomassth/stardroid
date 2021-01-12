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
import android.text.Spannable;

import androidx.annotation.NonNull;

/**
 * An abstract astronomical object.
 */
public abstract class CatalogEntry implements Comparable<CatalogEntry> {

    /**
     * His coordinates.
     */
    protected CatalogCoordinates coord;
    /**
     * His name.
     */
    protected String name;

    /**
     * @return the stored coordinates.
     */
    public CatalogCoordinates getCoordinates() {
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
     * @param ctx Context (to access resource strings)
     * @return description Spannable
     */
    public abstract Spannable createDescription(Context ctx);

    /**
     * Create the summary rich-text string (1 line)
     *
     * @param ctx Context (to access resource strings)
     * @return summary Spannable
     */
    public abstract Spannable createSummary(Context ctx);

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