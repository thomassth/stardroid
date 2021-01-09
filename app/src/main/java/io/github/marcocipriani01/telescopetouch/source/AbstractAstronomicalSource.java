package io.github.marcocipriani01.telescopetouch.source;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * Base implementation of the {@link AstronomicalSource} and {@link Sources}
 * interfaces.
 *
 * @author Brent Bryan
 */
public abstract class AbstractAstronomicalSource implements AstronomicalSource, Sources {

    @Override
    public Sources initialize() {
        return this;
    }

    @Override
    public EnumSet<UpdateType> update() {
        return EnumSet.noneOf(UpdateType.class);
    }

    /**
     * Implementors of this method must implement {@link #getSearchLocation}.
     */
    @Override
    public List<String> getNames() {
        return Collections.emptyList();
    }

    @Override
    public GeocentricCoordinates getSearchLocation() {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public List<? extends ImageSource> getImages() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends TextSource> getLabels() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends LineSource> getLines() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends PointSource> getPoints() {
        return Collections.emptyList();
    }
}
