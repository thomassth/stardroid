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

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.source.impl.LineSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.TextSourceImpl;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.RaDec;

/**
 * Creates a Layer which returns Sources which correspond to grid lines parallel
 * to the celestial equator and the hour angle. That is, returns a set of lines
 * with constant right ascension, and another set with constant declination.
 *
 * @author Brent Bryan
 * @author John Taylor
 */
public class GridLayer extends AbstractLayer {

    private final int numRightAscentionLines;
    private final int numDeclinationLines;

    /**
     * @param numDeclinationLines The number of declination lines to show including the poles
     *                            on each side of the equator. 9 is a good number for 10 degree
     *                            intervals.
     */
    public GridLayer(Resources resources, int numRightAscentionLines, int numDeclinationLines) {
        super(resources, false);
        this.numRightAscentionLines = numRightAscentionLines;
        this.numDeclinationLines = numDeclinationLines;
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
        sources.add(new GridSource(getResources(), numRightAscentionLines, numDeclinationLines));
    }

    @Override
    public int getLayerDepthOrder() {
        return 0;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.show_grid_pref;
    }

    @Override
    public String getPreferenceId() {
        return "source_provider.4";
    }

    /**
     * Implementation of the grid elements as an {@link AstronomicalSource}
     */
    @SuppressLint("DefaultLocale")
    static class GridSource extends AstronomicalSource {
        private static final int LINE_COLOR = Color.argb(20, 248, 239, 188);
        /**
         * These are great (semi)circles, so only need 3 points.
         */
        private static final int NUM_DEC_VERTICES = 3;
        /**
         * every 10 degrees
         */
        private static final int NUM_RA_VERTICES = 36;

        private final ArrayList<LineSourceImpl> lineSources = new ArrayList<>();
        private final ArrayList<TextSourceImpl> textSources = new ArrayList<>();

        public GridSource(Resources res, int numRaSources, int numDecSources) {
            for (int r = 0; r < numRaSources; r++) {
                lineSources.add(createRaLine(r, numRaSources));
            }

            // North & South pole, hour markers every 2hrs.
            textSources.add(new TextSourceImpl(0f, 90f, res.getString(R.string.north_pole), LINE_COLOR));
            textSources.add(new TextSourceImpl(0f, -90f, res.getString(R.string.south_pole), LINE_COLOR));
            for (int index = 0; index < 12; index++) {
                float ra = index * 30.0f;
                String title = String.format("%dh", 2 * index);
                textSources.add(new TextSourceImpl(ra, 0.0f, title, LINE_COLOR));
            }

            lineSources.add(createDecLine(0)); // Equator
            // Note that we don't create lines at the poles.
            for (int d = 1; d < numDecSources; d++) {
                float dec = d * 90.0f / numDecSources;
                lineSources.add(createDecLine(dec));
                textSources.add(new TextSourceImpl(0f, dec, String.format("%d°", (int) dec), LINE_COLOR));
                lineSources.add(createDecLine(-dec));
                textSources.add(new TextSourceImpl(0f, -dec, String.format("%d°", (int) -dec), LINE_COLOR));
            }
        }

        /**
         * Constructs a single longitude line. These lines run from the north pole to
         * the south pole at fixed Right Ascensions.
         */
        private LineSourceImpl createRaLine(int index, int numRaSources) {
            LineSourceImpl line = new LineSourceImpl(LINE_COLOR);
            float ra = index * 360.0f / numRaSources;
            for (int i = 0; i < NUM_DEC_VERTICES - 1; i++) {
                float dec = 90.0f - i * 180.0f / (NUM_DEC_VERTICES - 1);
                RaDec raDec = new RaDec(ra, dec);
                line.raDecs.add(raDec);
                line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            }
            RaDec raDec = new RaDec(0.0f, -90.0f);
            line.raDecs.add(raDec);
            line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            return line;
        }

        private LineSourceImpl createDecLine(float dec) {
            LineSourceImpl line = new LineSourceImpl(LINE_COLOR);
            for (int i = 0; i < NUM_RA_VERTICES; i++) {
                float ra = i * 360.0f / NUM_RA_VERTICES;
                RaDec raDec = new RaDec(ra, dec);
                line.raDecs.add(raDec);
                line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            }
            RaDec raDec = new RaDec(0.0f, dec);
            line.raDecs.add(raDec);
            line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            return line;
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