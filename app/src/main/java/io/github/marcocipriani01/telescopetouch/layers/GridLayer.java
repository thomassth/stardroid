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

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * Creates a Layer which returns Sources which correspond to grid lines parallel
 * to the celestial equator and the hour angle. That is, returns a set of lines
 * with constant right ascension, and another set with constant declination.
 *
 * @author Brent Bryan
 * @author John Taylor
 */
public class GridLayer extends AbstractLayer {

    public static final int DEPTH_ORDER = 30;
    public static final String PREFERENCE_ID = "source_provider.4";
    private final int raLinesCount;
    private final int decLinesCount;

    /**
     * @param decLinesCount The number of declination lines to show including the poles
     *                      on each side of the equator. 9 is a good number for 10 degree
     *                      intervals.
     */
    public GridLayer(Resources resources, int raLinesCount, int decLinesCount) {
        super(resources, false);
        this.raLinesCount = raLinesCount;
        this.decLinesCount = decLinesCount;
    }

    @Override
    protected void initializeAstroSources(List<AstronomicalSource> sources) {
        sources.add(new GridSource(getResources(), raLinesCount, decLinesCount));
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
        private final List<LineSource> lineSources = Collections.synchronizedList(new ArrayList<>());
        private final List<TextSource> textSources = Collections.synchronizedList(new ArrayList<>());

        public GridSource(Resources res, int numRaSources, int numDecSources) {
            for (int r = 0; r < numRaSources; r++) {
                lineSources.add(createRaLine(r, numRaSources));
            }

            // North & South pole, hour markers every 2hrs.
            textSources.add(new TextSource(0f, 90f, res.getString(R.string.north_pole), LINE_COLOR));
            textSources.add(new TextSource(0f, -90f, res.getString(R.string.south_pole), LINE_COLOR));
            for (int index = 0; index < 12; index++) {
                float ra = index * 30.0f;
                String title = String.format("%dh", 2 * index);
                textSources.add(new TextSource(ra, 0.0f, title, LINE_COLOR));
            }

            lineSources.add(createDecLine(0)); // Equator
            // Note that we don't create lines at the poles.
            for (int d = 1; d < numDecSources; d++) {
                double dec = d * 90.0f / numDecSources;
                lineSources.add(createDecLine(dec));
                textSources.add(new TextSource(0f, (float) dec, String.format("%d°", (int) dec), LINE_COLOR));
                lineSources.add(createDecLine(-dec));
                textSources.add(new TextSource(0f, (float) -dec, String.format("%d°", (int) -dec), LINE_COLOR));
            }
        }

        /**
         * Constructs a single longitude line. These lines run from the north pole to
         * the south pole at fixed Right Ascensions.
         */
        private LineSource createRaLine(int index, int numRaSources) {
            LineSource line = new LineSource(LINE_COLOR);
            double ra = index * 360.0 / numRaSources;
            for (int i = 0; i < NUM_DEC_VERTICES - 1; i++) {
                double dec = 90.0 - i * 180.0 / (NUM_DEC_VERTICES - 1);
                EquatorialCoordinates raDec = new EquatorialCoordinates(ra, dec);
                line.raDecs.add(raDec);
                line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            }
            EquatorialCoordinates raDec = new EquatorialCoordinates(0.0, -90.0);
            line.raDecs.add(raDec);
            line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            return line;
        }

        private LineSource createDecLine(double dec) {
            LineSource line = new LineSource(LINE_COLOR);
            for (int i = 0; i < NUM_RA_VERTICES; i++) {
                double ra = i * 360.0 / NUM_RA_VERTICES;
                EquatorialCoordinates raDec = new EquatorialCoordinates(ra, dec);
                line.raDecs.add(raDec);
                line.vertices.add(GeocentricCoordinates.getInstance(raDec));
            }
            EquatorialCoordinates raDec = new EquatorialCoordinates(0.0, dec);
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