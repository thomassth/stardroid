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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.TimeUtils;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * Creates a mark at the zenith, nadir and cardinal point and a horizon.
 *
 * @author Brent Bryan
 * @author John Taylor
 */
public class HorizonLayer extends AbstractLayer {

    public static final int DEPTH_ORDER = 40;
    public static final String PREFERENCE_ID = "source_provider.5";
    private final AstronomerModel model;

    public HorizonLayer(AstronomerModel model, Resources resources) {
        super(resources, true);
        this.model = model;
    }

    @Override
    protected void initializeAstroSources(List<AstronomicalSource> sources) {
        sources.add(new HorizonSource(model, getResources()));
    }

    @Override
    public int getLayerDepthOrder() {
        return DEPTH_ORDER;
    }

    @Override
    public String getPreferenceId() {
        return PREFERENCE_ID;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.horizon;
    }

    /**
     * Implementation of {@link AstronomicalSource} for the horizon source.
     */
    static class HorizonSource extends AstronomicalSource {

        // Due to a bug in the G1 rendering code text and lines render in different
        // colors.
        private static final int LINE_COLOR = Color.argb(120, 64, 196, 255);
        private static final int LABEL_COLOR = Color.argb(120, 255, 171, 64);
        private static final long UPDATE_FREQ_MS = TimeUtils.MILLISECONDS_PER_SECOND;
        private final GeocentricCoordinates zenith = new GeocentricCoordinates();
        private final GeocentricCoordinates nadir = new GeocentricCoordinates();
        private final GeocentricCoordinates north = new GeocentricCoordinates();
        private final GeocentricCoordinates south = new GeocentricCoordinates();
        private final GeocentricCoordinates east = new GeocentricCoordinates();
        private final GeocentricCoordinates west = new GeocentricCoordinates();
        private final List<LineSource> lineSources = Collections.synchronizedList(new ArrayList<>());
        private final List<TextSource> textSources = Collections.synchronizedList(new ArrayList<>());
        private final AstronomerModel model;
        private long lastUpdateTimeMs = 0L;

        public HorizonSource(AstronomerModel model, Resources res) {
            this.model = model;

            List<GeocentricCoordinates> vertices = Arrays.asList(north, east, south, west, north);
            lineSources.add(new LineSource(LINE_COLOR, vertices, 1.5f));

            textSources.add(new TextSource(zenith, res.getString(R.string.zenith), LABEL_COLOR));
            textSources.add(new TextSource(nadir, res.getString(R.string.nadir), LABEL_COLOR));
            textSources.add(new TextSource(north, res.getString(R.string.north), LABEL_COLOR));
            textSources.add(new TextSource(south, res.getString(R.string.south), LABEL_COLOR));
            textSources.add(new TextSource(east, res.getString(R.string.east), LABEL_COLOR));
            textSources.add(new TextSource(west, res.getString(R.string.west), LABEL_COLOR));
        }

        private void updateCoords() {
            // Blog.d(this, "Updating Coords: " + (model.getTime().getTime() - lastUpdateTimeMs));

            this.lastUpdateTimeMs = model.getTime().getTimeInMillis();
            this.zenith.assign(model.getZenith());
            this.nadir.assign(model.getNadir());
            this.north.assign(model.getNorth());
            this.south.assign(model.getSouth());
            this.east.assign(model.getEast());
            this.west.assign(model.getWest());
        }

        @Override
        public AstronomicalSource initialize() {
            updateCoords();
            return this;
        }

        @Override
        public EnumSet<UpdateType> update() {
            EnumSet<UpdateType> updateTypes = EnumSet.noneOf(UpdateType.class);
            // TODO(brent): Add distance here.
            if (Math.abs(model.getTime().getTimeInMillis() - lastUpdateTimeMs) > UPDATE_FREQ_MS) {
                updateCoords();
                updateTypes.add(UpdateType.UpdatePositions);
            }
            return updateTypes;
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