package io.github.marcocipriani01.telescopetouch.gallery;

/**
 * Holds data about an image.
 *
 * @author John Taylor
 */
public class GalleryImage {

    public int imageId;
    public String name;
    public String searchTerm;

    public GalleryImage(int imageId, String name, String searchTerm) {
        this.imageId = imageId;
        this.name = name;
        this.searchTerm = searchTerm;
    }
}