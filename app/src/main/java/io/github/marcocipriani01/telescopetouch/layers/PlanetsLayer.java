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

package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.Planet;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.Sources;
import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.source.impl.ImageSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.TextSourceImpl;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

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
    private static final int LABEL_COLOR = 0xf67e81;
    private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);
    private final AstronomerModel model;

    public PlanetsLayer(AstronomerModel model, Resources resources) {
        super(resources, true);
        this.model = model;
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
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

        private final ArrayList<PointSource> pointSources = new ArrayList<>();
        private final ArrayList<ImageSourceImpl> imageSources = new ArrayList<>();
        private final ArrayList<TextSource> labelSources = new ArrayList<>();
        private final Planet planet;
        private final String name;
        private final GeocentricCoordinates currentCoords = new GeocentricCoordinates(0, 0, 0);
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
            for (ImageSourceImpl imageSource : imageSources) {
                imageSource.setUpVector(sunCoords);  // TODO(johntaylor): figure out why we do this.
            }
        }

        @Override
        public Sources initialize() {
            Calendar time = model.getTime();
            updateCoords(time);
            this.imageId = planet.getImageResourceId(time);
            Resources resources = getResources();
            if (planet == Planet.Moon) {
                imageSources.add(new ImageSourceImpl(currentCoords, resources, imageId, sunCoords, planet.getPlanetaryImageSize()));
            } else {
                imageSources.add(new ImageSourceImpl(currentCoords, resources, imageId, UP, planet.getPlanetaryImageSize()));
            }
            labelSources.add(new TextSourceImpl(currentCoords, name, LABEL_COLOR));
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
                    int newImageId = planet.getImageResourceId(modelTime);
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