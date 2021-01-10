package io.github.marcocipriani01.telescopetouch.source;

import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * This class represents a single source shown in SkyMap. An AstronomicalSource
 * may consist of several components. For instance, a constellation may have a
 * label, an image, as well as the star to star lines.
 *
 * @author Brent Bryan
 */
public interface AstronomicalSource {
    /**
     * Returns a list of names associated with this source. Names in this list
     * should be internationalized.
     */
    List<String> getNames();

    /**
     * Returns the {@link GeocentricCoordinates} of the center of this object.
     * This is the point to which the user will be directed for a search.
     */
    GeocentricCoordinates getSearchLocation();

    /*
      Returns the zoom level to which the user should be taken (in manual mode)
      to completely see this object when searching.
     */
    // float getSearchLevel();

    /*
      Returns the level associated with this source. Levels typically corresponds
      to the magnitude of the object and dictate whether the source will be shown
      given the zoom level and limits set by the user.
     */
    // float getLevel();

    /**
     * Initializes and returns the elements for this {@link AstronomicalSource}.
     * Elements should have their positions, images, etc update to the current
     * time / location information.
     */
    Sources initialize();

    /**
     * Updates the {@link Sources} of this {@link AstronomicalSource} in response
     * to a change in the user's location or current time. Changes can be caused
     * by the user moving to a new location or time progressing, or by the user
     * manually selecting a different location. Returns the minimal Set of
     * UpdateType required to enact the changes required by this update.
     */
    EnumSet<UpdateType> update();
}
