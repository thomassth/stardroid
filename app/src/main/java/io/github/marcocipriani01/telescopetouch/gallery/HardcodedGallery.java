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

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * A collection of gallery images.
 *
 * @author John Taylor
 */
public class HardcodedGallery implements Gallery {

    private final List<GalleryImage> images;
    private final Resources resources;

    HardcodedGallery(Resources resources) {
        this.resources = resources;
        images = Collections.unmodifiableList(createImages());
    }

    public List<GalleryImage> getGalleryImages() {
        return images;
    }

    /**
     * Adds an image to the gallery, but using an internationalized search term.
     * Note, that for this to work the internationalized name _must_ be in the
     * search index.
     */
    private void add(ArrayList<GalleryImage> images, int imageId, int nameId, int searchTermId) {
        images.add(new GalleryImage(imageId, getString(nameId), getString(searchTermId)));
    }

    private void add(ArrayList<GalleryImage> images, int imageId, int nameId, int searchTermId, String gotoName) {
        images.add(new GalleryImage(imageId, getString(nameId), getString(searchTermId), gotoName));
    }

    private ArrayList<GalleryImage> createImages() {
        ArrayList<GalleryImage> galleryImages = new ArrayList<>();
        // Note the internationalized names in places.  Be sure that if the
        // search term is internationalized in the search index then it is here too.
        add(galleryImages, R.drawable.gallery_mercury, R.string.mercury, R.string.mercury);
        add(galleryImages, R.drawable.gallery_venus, R.string.venus, R.string.venus);
        add(galleryImages, R.drawable.gallery_mars, R.string.mars, R.string.mars);
        add(galleryImages, R.drawable.gallery_jupiter, R.string.jupiter, R.string.jupiter);
        add(galleryImages, R.drawable.gallery_saturn, R.string.saturn, R.string.saturn);
        add(galleryImages, R.drawable.gallery_uranus, R.string.uranus, R.string.uranus);
        add(galleryImages, R.drawable.gallery_neptune, R.string.neptune, R.string.neptune);
        add(galleryImages, R.drawable.gallery_pluto, R.string.pluto, R.string.pluto);
        add(galleryImages, R.drawable.gallery_crab, R.string.crab_nebula, R.string.crab_nebula, "m1");
        add(galleryImages, R.drawable.gallery_m13, R.string.hercules_gc, R.string.hercules_gc, "m13");
        add(galleryImages, R.drawable.gallery_eagle, R.string.eagle_nebula, R.string.eagle_nebula, "m16");
        add(galleryImages, R.drawable.gallery_andromeda, R.string.andromeda_galaxy, R.string.andromeda_galaxy, "m31");
        add(galleryImages, R.drawable.gallery_pleiades, R.string.pleiades, R.string.pleiades, "m45");
        add(galleryImages, R.drawable.gallery_m51, R.string.whirlpool_galaxy, R.string.whirlpool_galaxy, "m51");
        add(galleryImages, R.drawable.gallery_ring_nebula, R.string.ring_nebula, R.string.ring_nebula, "m57");
        add(galleryImages, R.drawable.gallery_pinwheel, R.string.pinwheel_galaxy, R.string.pinwheel_galaxy, "m101");
        add(galleryImages, R.drawable.gallery_sombrero, R.string.sombrero_galaxy, R.string.sombrero_galaxy, "m104");
        add(galleryImages, R.drawable.gallery_cats_eye, R.string.cats_eye_nebula, R.string.cats_eye_nebula, "ngc6543");
        add(galleryImages, R.drawable.gallery_omega_centauri, R.string.omega_centauri, R.string.omega_centauri, "ngc5139");
        add(galleryImages, R.drawable.gallery_m42, R.string.orion_nebula, R.string.m42, "m42");
        add(galleryImages, R.drawable.gallery_hdf, R.string.hubble_deep_field, R.string.hubble_deep_field, "?");
        add(galleryImages, R.drawable.gallery_v838_mon, R.string.v838_mon, R.string.v838_mon, "?");
        return galleryImages;
    }

    private String getString(int id) {
        return resources.getString(id);
    }
}