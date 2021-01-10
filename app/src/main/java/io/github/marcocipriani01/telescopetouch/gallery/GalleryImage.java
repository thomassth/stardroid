package io.github.marcocipriani01.telescopetouch.gallery;

/**
 * Holds data about an image.
 *
 * @author John Taylor
 */
public class GalleryImage {

    public final int imageId;
    public final String name;
    public final String searchTerm;

    public GalleryImage(int imageId, String name, String searchTerm) {
        this.imageId = imageId;
        this.name = name;
        this.searchTerm = searchTerm;
    }
}