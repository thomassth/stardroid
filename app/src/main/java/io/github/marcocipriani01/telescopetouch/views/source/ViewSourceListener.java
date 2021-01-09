package io.github.marcocipriani01.telescopetouch.views.source;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * Defines a simple listener interface which responds to changes in the sources and
 * updates the corresponding views.
 *
 * @author Brent Bryan
 */
public interface ViewSourceListener {

    /**
     * Sets all of the PointSources for a specific id in response to one or more
     * source changing values. Changes include addition of new object, updates of
     * current objects, or deletion of current objects. Any PointSources set
     * previously with the same id value will be overwritten.
     *
     * @param id a unique identify integer for this set of point sources.
     * @param s  a list of PointSources which should be used.
     */
    void setPointSources(int id, List<PointSource> s);

    /**
     * Sets all of the TextSources for a specific id in response to one or more
     * source changing values. Changes include addition of new object, updates of
     * current objects, or deletion of current objects. Any TextSources set
     * previously with the same id value will be overwritten.
     *
     * @param id a unique identify integer for this set of point sources.
     * @param s  a list of TextSources which should be used.
     */
    void setTextSources(int id, List<TextSource> s);

    /**
     * Sets all of the ImageSources for a specific id in response to one or more
     * source changing values. Changes include addition of new object, updates of
     * current objects, or deletion of current objects. Any ImageSources set
     * previously with the same id value will be overwritten.
     *
     * @param id a unique identify integer for this set of point sources.
     * @param s  a list of ImageSources which should be used.
     */
    void setImageSources(int id, List<ImageSource> s);

    /**
     * Sets all of the PolyLineSources for a specific id in response to one or more
     * source changing values. Changes include addition of new object, updates of
     * current objects, or deletion of current objects. Any PolyLineSources set
     * previously with the same id value will be overwritten.
     *
     * @param id a unique identify integer for this set of point sources.
     * @param s  a list of PolyLineSources which should be used.
     */
    void setPolyLineSources(int id, List<LineSource> s);
}
