/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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

package io.github.marcocipriani01.telescopetouch.gallery;

import android.content.Context;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * A Gallery contains a list of images.
 *
 * @author John Taylor
 * @author marcocipriani01
 */
public enum GalleryImages {
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

    GalleryImages(int imageId, int name, int searchTerm) {
        this.imageId = imageId;
        this.name = name;
        this.searchTerm = searchTerm;
        this.gotoName = null;
    }

    GalleryImages(int imageId, int name, int searchTerm, String gotoName) {
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