/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.gallery;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * A Gallery contains a list of images.
 *
 * @author John Taylor
 * @author marcocipriani01
 */
public enum GalleryImages {
    SUN(R.drawable.gallery_sun, R.string.sun, R.string.sun),
    MERCURY(R.drawable.gallery_mercury, R.string.mercury, R.string.mercury),
    VENUS(R.drawable.gallery_venus, R.string.venus, R.string.venus),
    MARS(R.drawable.gallery_mars, R.string.mars, R.string.mars),
    JUPITER(R.drawable.gallery_jupiter, R.string.jupiter, R.string.jupiter),
    SATURN(R.drawable.gallery_saturn, R.string.saturn, R.string.saturn),
    URANUS(R.drawable.gallery_uranus, R.string.uranus, R.string.uranus),
    NEPTUNE(R.drawable.gallery_neptune, R.string.neptune, R.string.neptune),
    PLUTO(R.drawable.gallery_pluto, R.string.pluto, R.string.pluto),
    M1(R.drawable.gallery_crab, R.string.crab_nebula, R.string.crab_nebula, "m1"),
    M13(R.drawable.gallery_m13, R.string.hercules_gc, R.string.hercules_gc, "m13"),
    M16(R.drawable.gallery_eagle, R.string.eagle_nebula, R.string.eagle_nebula, "m16"),
    M31(R.drawable.gallery_andromeda, R.string.andromeda_galaxy, R.string.andromeda_galaxy, "m31"),
    M45(R.drawable.gallery_pleiades, R.string.pleiades, R.string.pleiades, "m45"),
    M51(R.drawable.gallery_m51, R.string.whirlpool_galaxy, R.string.whirlpool_galaxy, "m51"),
    M57(R.drawable.gallery_ring_nebula, R.string.ring_nebula, R.string.ring_nebula, "m57"),
    M101(R.drawable.gallery_pinwheel, R.string.pinwheel_galaxy, R.string.pinwheel_galaxy, "m101"),
    M104(R.drawable.gallery_sombrero, R.string.sombrero_galaxy, R.string.sombrero_galaxy, "m104"),
    NGC6543(R.drawable.gallery_cats_eye, R.string.cats_eye_nebula, R.string.cats_eye_nebula, "ngc6543"),
    NGC5139(R.drawable.gallery_omega_centauri, R.string.omega_centauri, R.string.omega_centauri, "ngc5139"),
    M42(R.drawable.gallery_m42, R.string.orion_nebula, R.string.m42, "m42"),
    HDF(R.drawable.gallery_hdf, R.string.hubble_deep_field, R.string.hubble_deep_field, "?"),
    V838_MON(R.drawable.gallery_v838_mon, R.string.v838_mon, R.string.v838_mon, "?");

    private final int imageId;
    private final int name;
    private final int searchTerm;
    private final String gotoName;

    GalleryImages(@DrawableRes int imageId, @StringRes int name, @StringRes int searchTerm) {
        this.imageId = imageId;
        this.name = name;
        this.searchTerm = searchTerm;
        this.gotoName = null;
    }

    GalleryImages(@DrawableRes int imageId, @StringRes int name, @StringRes int searchTerm, String gotoName) {
        this.imageId = imageId;
        this.name = name;
        this.searchTerm = searchTerm;
        this.gotoName = gotoName;
    }

    public int getImageId() {
        return imageId;
    }

    public String getName(Context context) {
        return context.getString(name);
    }

    public String getSearchTerm(Context context) {
        return context.getString(searchTerm);
    }

    public String getGotoName(Context context) {
        return (gotoName == null) ? context.getString(searchTerm) : gotoName;
    }
}