package io.github.marcocipriani01.telescopetouch.gallery;

import android.content.res.Resources;

/**
 * Constructs galleries.
 *
 * @author John Taylor
 */
public class GalleryFactory {
    private static Gallery gallery;

    private GalleryFactory() {
    }

    /**
     * Returns the gallery.  This will usually be a singleton.
     */
    public static synchronized Gallery getGallery(Resources resources) {
        if (gallery == null) {
            gallery = new HardcodedGallery(resources);
        }
        return gallery;
    }
}
