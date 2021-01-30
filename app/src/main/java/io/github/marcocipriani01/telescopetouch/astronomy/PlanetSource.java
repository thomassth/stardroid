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

package io.github.marcocipriani01.telescopetouch.astronomy;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.source.AbstractAstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.Sources;
import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.source.impl.ImageSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.PointSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.TextSourceImpl;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.RaDec;
import io.github.marcocipriani01.telescopetouch.units.Vector3;

/**
 * Implementation of the
 * {@link AstronomicalSource} for planets.
 *
 * @author Brent Bryan
 */
public class PlanetSource extends AbstractAstronomicalSource {

    private static final int PLANET_SIZE = 3;
    private static final int PLANET_COLOR = Color.argb(20, 129, 126, 246);
    private static final int PLANET_LABEL_COLOR = 0xf67e81;
    private static final String SHOW_PLANETARY_IMAGES = "show_planetary_images";
    private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);

    private final ArrayList<PointSource> pointSources = new ArrayList<>();
    private final ArrayList<ImageSourceImpl> imageSources = new ArrayList<>();
    private final ArrayList<TextSource> labelSources = new ArrayList<>();
    private final Planet planet;
    private final Resources resources;
    private final AstronomerModel model;
    private final String name;
    private final SharedPreferences preferences;
    private final GeocentricCoordinates currentCoords = new GeocentricCoordinates(0, 0, 0);
    private HeliocentricCoordinates sunCoords;
    private int imageId = -1;

    private long lastUpdateTimeMs = 0L;

    public PlanetSource(Planet planet, Resources resources, AstronomerModel model, SharedPreferences prefs) {
        this.planet = planet;
        this.resources = resources;
        this.model = model;
        this.name = resources.getString(planet.getNameResourceId());
        this.preferences = prefs;
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
        this.currentCoords.updateFromRaDec(RaDec.getInstance(planet, time, sunCoords));
        for (ImageSourceImpl imageSource : imageSources) {
            imageSource.setUpVector(sunCoords);  // TODO(johntaylor): figure out why we do this.
        }
    }

    @Override
    public Sources initialize() {
        Calendar time = model.getTime();
        updateCoords(time);
        this.imageId = planet.getImageResourceId(time);

        if (planet == Planet.Moon) {
            imageSources.add(new ImageSourceImpl(currentCoords, resources, imageId, sunCoords,
                    planet.getPlanetaryImageSize()));
        } else {
            boolean usePlanetaryImages = preferences.getBoolean(SHOW_PLANETARY_IMAGES, true);
            if (usePlanetaryImages || planet == Planet.Sun) {
                imageSources.add(new ImageSourceImpl(currentCoords, resources, imageId, UP,
                        planet.getPlanetaryImageSize()));
            } else {
                pointSources.add(new PointSourceImpl(currentCoords, PLANET_COLOR, PLANET_SIZE));
            }
        }
        labelSources.add(new TextSourceImpl(currentCoords, name, PLANET_LABEL_COLOR));

        return this;
    }

    @Override
    public EnumSet<UpdateType> update() {
        EnumSet<UpdateType> updates = EnumSet.noneOf(UpdateType.class);

        Calendar modelTime = model.getTime();
        if (Math.abs(modelTime.getTimeInMillis() - lastUpdateTimeMs) > planet.getUpdateFrequencyMs()) {
            updates.add(UpdateType.UpdatePositions);
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
                    updates.add(UpdateType.UpdateImages);
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
