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
import android.util.Log;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.HeliocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.Planet;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererController;
import io.github.marcocipriani01.telescopetouch.search.SearchResult;

/**
 * If enabled, keeps the sky gradient up to date.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class SkyGradientLayer implements Layer {

    public static final int DEPTH_ORDER = 0;
    public static final String PREFERENCE_ID = "source_provider.sky_gradient";
    private static final String TAG = TelescopeTouchApp.getTag(SkyGradientLayer.class);
    private final ReentrantLock rendererLock = new ReentrantLock();
    private final AstronomerModel model;
    private final Resources resources;
    private RendererController renderer;

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
        Calendar time = model.getTime();
        EquatorialCoordinates sunPosition = Planet.Sun.getEquatorialCoordinates(time, HeliocentricCoordinates.getInstance(Planet.Sun, time));
        // Log.d(TAG, "Enabling sky gradient with sun position " + sunPosition);
        rendererLock.lock();
        try {
            renderer.queueEnableSkyGradient(GeocentricCoordinates.getInstance(sunPosition));
        } finally {
            rendererLock.unlock();
        }
    }

    @Override
    public int getLayerDepthOrder() {
        return DEPTH_ORDER;
    }

    public String getPreferenceId() {
        return PREFERENCE_ID;
    }

    public String getLayerName() {
        return resources.getString(R.string.show_sky_gradient);
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