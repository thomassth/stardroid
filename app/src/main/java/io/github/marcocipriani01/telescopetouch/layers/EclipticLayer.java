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
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * Creates a Layer for the Ecliptic.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class EclipticLayer extends AbstractLayer {

    public static final int DEPTH_ORDER = 50;
    public static final String PREFERENCE_ID = "source_provider.4";

    public EclipticLayer(Resources resources) {
        super(resources, false);
    }

    @Override
    protected void initializeAstroSources(List<AstronomicalSource> sources) {
        sources.add(new EclipticSource(getResources()));
    }

    @Override
    public int getLayerDepthOrder() {
        return DEPTH_ORDER;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.show_grid_pref;
    }

    @Override
    public String getPreferenceId() {
        return PREFERENCE_ID;
    }

    /**
     * Implementation of {@link AstronomicalSource} for the ecliptic source.
     */
    private static class EclipticSource extends AstronomicalSource {

        // Earth's Angular Tilt
        private static final float EPSILON = 23.439281f;
        private static final int LINE_COLOR = Color.argb(20, 255, 109, 0);
        private final List<LineSource> lineSources = Collections.synchronizedList(new ArrayList<>());
        private final List<TextSource> textSources = Collections.synchronizedList(new ArrayList<>());

        public EclipticSource(Resources res) {
            String title = res.getString(R.string.ecliptic);
            textSources.add(new TextSource(90.0f, EPSILON, title, LINE_COLOR));
            textSources.add(new TextSource(270f, -EPSILON, title, LINE_COLOR));

            // Create line source.
            float[] ra = {0, 90, 180, 270, 0};
            float[] dec = {0, EPSILON, 0, -EPSILON, 0};

            ArrayList<GeocentricCoordinates> vertices = new ArrayList<>();
            for (int i = 0; i < ra.length; ++i) {
                vertices.add(GeocentricCoordinates.getInstance(ra[i], dec[i]));
            }
            lineSources.add(new LineSource(LINE_COLOR, vertices, 1.5f));
        }

        @Override
        public List<? extends TextSource> getLabels() {
            return textSources;
        }

        @Override
        public List<? extends LineSource> getLines() {
            return lineSources;
        }
    }
}