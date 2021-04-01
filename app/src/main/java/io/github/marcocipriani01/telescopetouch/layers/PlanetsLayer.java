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

package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.Planet;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * An implementation of the {@link Layer} interface for displaying planets in
 * the Renderer.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class PlanetsLayer extends AbstractLayer {

    public static final int DEPTH_ORDER = 60;
    public static final String PREFERENCE_ID = "source_provider.3";
    private static final int LABEL_COLOR = 0xFF3D00;
    private final AstronomerModel model;

    public PlanetsLayer(AstronomerModel model, Resources resources) {
        super(resources, true);
        this.model = model;
    }

    @Override
    protected void initializeAstroSources(List<AstronomicalSource> sources) {
        for (Planet planet : Planet.values()) {
            sources.add(new PlanetSource(planet));
        }
    }

    public String getPreferenceId() {
        return PREFERENCE_ID;
    }

    @Override
    public int getLayerDepthOrder() {
        return DEPTH_ORDER;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.solar_system;
    }

    /**
     * Implementation of the
     * {@link AstronomicalSource} for planets.
     *
     * @author Brent Bryan
     */
    private class PlanetSource extends AstronomicalSource {

        private final List<PointSource> pointSources = Collections.synchronizedList(new ArrayList<>());
        private final List<ImageSource> imageSources = Collections.synchronizedList(new ArrayList<>());
        private final List<TextSource> labelSources = Collections.synchronizedList(new ArrayList<>());
        private final Planet planet;
        private final String name;
        private final GeocentricCoordinates currentCoords = new GeocentricCoordinates();
        private HeliocentricCoordinates sunCoords;
        private int imageId = -1;
        private long lastUpdateTimeMs = 0L;

        public PlanetSource(Planet planet) {
            this.planet = planet;
            this.name = planet.getName(getResources());
        }

        @Override
        public List<String> getNames() {
            return Collections.singletonList(name);
        }

        @Override
        public GeocentricCoordinates getSearchLocation() {
            return currentCoords;
        }

        private void updateCoords(Calendar time) {
            this.lastUpdateTimeMs = time.getTimeInMillis();
            this.sunCoords = HeliocentricCoordinates.getInstance(Planet.Sun, time);
            this.currentCoords.updateFromRaDec(planet.getEquatorialCoordinates(time, sunCoords));
            for (ImageSource imageSource : imageSources) {
                imageSource.setUpVector(sunCoords);  // TODO(johntaylor): figure out why we do this.
            }
        }

        @Override
        public AstronomicalSource initialize() {
            Calendar time = model.getTime();
            updateCoords(time);
            this.imageId = planet.getMapResourceId(time);
            Resources resources = getResources();
            if (planet == Planet.Moon) {
                imageSources.add(new ImageSource(currentCoords, resources, imageId, sunCoords, planet.getPlanetaryImageSize()));
            } else {
                imageSources.add(new ImageSource(currentCoords, resources, imageId, planet.getPlanetaryImageSize()));
            }
            labelSources.add(new TextSource(currentCoords, name, LABEL_COLOR));
            return this;
        }

        @Override
        public EnumSet<RendererObjectManager.UpdateType> update() {
            EnumSet<RendererObjectManager.UpdateType> updates = EnumSet.noneOf(RendererObjectManager.UpdateType.class);
            Calendar modelTime = model.getTime();
            if (Math.abs(modelTime.getTimeInMillis() - lastUpdateTimeMs) > planet.getUpdateFrequencyMs()) {
                updates.add(RendererObjectManager.UpdateType.UpdatePositions);
                // update location
                updateCoords(modelTime);
                // For moon only:
                if (planet == Planet.Moon && !imageSources.isEmpty()) {
                    // Update up vector.
                    imageSources.get(0).setUpVector(sunCoords);
                    // update image:
                    int newImageId = planet.getMapResourceId(modelTime);
                    if (newImageId != imageId) {
                        imageId = newImageId;
                        imageSources.get(0).setImageId(imageId);
                        updates.add(RendererObjectManager.UpdateType.UpdateImages);
                    }
                }
            }
            return updates;
        }

        @Override
        public List<? extends ImageSource> getImages() {
            return imageSources;
        }

        @Override
        public List<? extends TextSource> getLabels() {
            return labelSources;
        }

        @Override
        public List<? extends PointSource> getPoints() {
            return pointSources;
        }
    }
}