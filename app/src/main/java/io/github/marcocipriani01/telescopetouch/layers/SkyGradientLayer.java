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
import android.util.Log;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.ephemeris.SolarPositionCalculator;
import io.github.marcocipriani01.telescopetouch.renderer.RendererController;
import io.github.marcocipriani01.telescopetouch.search.SearchResult;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.RaDec;
import io.github.marcocipriani01.telescopetouch.util.AstroTimeUtils;

/**
 * If enabled, keeps the sky gradient up to date.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class SkyGradientLayer implements Layer {

    private static final String TAG = TelescopeTouchApp.getTag(SkyGradientLayer.class);
    private static final long UPDATE_FREQUENCY_MS = 5L * AstroTimeUtils.MILLISECONDS_PER_MINUTE;

    private final ReentrantLock rendererLock = new ReentrantLock();
    private final AstronomerModel model;
    private final Resources resources;

    private RendererController renderer;
    private long lastUpdateTimeMs = 0L;

    public SkyGradientLayer(AstronomerModel model, Resources resources) {
        this.model = model;
        this.resources = resources;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void registerWithRenderer(RendererController controller) {
        this.renderer = controller;
        redraw();
    }

    @Override
    public void setVisible(boolean visible) {
        Log.d(TAG, "Setting showSkyGradient " + visible);
        if (visible) {
            redraw();
        } else {
            rendererLock.lock();
            try {
                renderer.queueDisableSkyGradient();
            } finally {
                rendererLock.unlock();
            }
        }
    }

    /**
     * Redraws the sky shading gradient using the model's current time.
     */
    protected void redraw() {
        Calendar modelTime = model.getTime();
        long timeInMillis = modelTime.getTimeInMillis();
        if (Math.abs(timeInMillis - lastUpdateTimeMs) > UPDATE_FREQUENCY_MS) {
            lastUpdateTimeMs = timeInMillis;
            RaDec sunPosition = SolarPositionCalculator.getSolarPosition(modelTime);
            // Log.d(TAG, "Enabling sky gradient with sun position " + sunPosition);
            rendererLock.lock();
            try {
                renderer.queueEnableSkyGradient(GeocentricCoordinates.getInstance(sunPosition));
            } finally {
                rendererLock.unlock();
            }
        }
    }

    @Override
    public int getLayerDepthOrder() {
        return -10;
    }

    public String getPreferenceId() {
        return "source_provider." + getLayerNameId();
    }

    public String getLayerName() {
        return resources.getString(getLayerNameId());
    }

    private int getLayerNameId() {
        return R.string.show_sky_gradient;
    }

    @Override
    public List<SearchResult> searchByObjectName(String name) {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getObjectNamesMatchingPrefix(String prefix) {
        return Collections.emptySet();
    }
}