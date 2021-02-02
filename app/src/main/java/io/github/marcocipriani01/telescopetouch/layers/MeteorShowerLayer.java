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
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.source.AbstractAstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.Sources;
import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.source.impl.ImageSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.TextSourceImpl;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.util.TimeUtils;
import io.github.marcocipriani01.telescopetouch.util.Vector3;

/**
 * A {@link Layer} to show well-known meteor showers.
 *
 * @author John Taylor
 */
public class MeteorShowerLayer extends AbstractLayer {

    private static final int ANY_OLD_YEAR = 20000;
    /**
     * Number of meteors per hour for the larger graphic
     */
    private static final double METEOR_THRESHOLD_PER_HR = 10;
    private final AstronomerModel model;
    private final List<Shower> showers = new ArrayList<>();

    public MeteorShowerLayer(AstronomerModel model, Resources resources) {
        super(resources, true);
        this.model = model;
        initializeShowers();
    }

    private void initializeShowers() {
        // A list of all the meteor showers with > 10 per hour
        // Source: http://www.imo.net/calendar/2011#table5
        // Note the zero-based month. 10=November
        // Actual start for Quadrantids is December 28 - but we can't cross a year boundary.
        showers.add(new Shower(
                R.string.quadrantids, GeocentricCoordinates.getInstance(230, 49),
                new GregorianCalendar(ANY_OLD_YEAR, 0, 1),
                new GregorianCalendar(ANY_OLD_YEAR, 0, 4),
                new GregorianCalendar(ANY_OLD_YEAR, 0, 12),
                120));
        showers.add(new Shower(
                R.string.lyrids, GeocentricCoordinates.getInstance(271, 34),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 16),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 22),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 25),
                18));
        showers.add(new Shower(
                R.string.aquariids, GeocentricCoordinates.getInstance(338, -1),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 19),
                new GregorianCalendar(ANY_OLD_YEAR, 4, 6),
                new GregorianCalendar(ANY_OLD_YEAR, 4, 28),
                70));
        showers.add(new Shower(
                R.string.deltaaquariids, GeocentricCoordinates.getInstance(340, -16),
                new GregorianCalendar(ANY_OLD_YEAR, 6, 12),
                new GregorianCalendar(ANY_OLD_YEAR, 6, 30),
                new GregorianCalendar(ANY_OLD_YEAR, 7, 23),
                16));
        showers.add(new Shower(
                R.string.perseids, GeocentricCoordinates.getInstance(48, 58),
                new GregorianCalendar(ANY_OLD_YEAR, 6, 17),
                new GregorianCalendar(ANY_OLD_YEAR, 7, 13),
                new GregorianCalendar(ANY_OLD_YEAR, 7, 24),
                100));
        showers.add(new Shower(
                R.string.orionids, GeocentricCoordinates.getInstance(95, 16),
                new GregorianCalendar(ANY_OLD_YEAR, 9, 2),
                new GregorianCalendar(ANY_OLD_YEAR, 9, 21),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 7),
                25));
        showers.add(new Shower(
                R.string.leonids, GeocentricCoordinates.getInstance(152, 22),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 6),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 18),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 30),
                20));
        showers.add(new Shower(
                R.string.puppidvelids, GeocentricCoordinates.getInstance(123, -45),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 1),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 7),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 15),
                10));
        showers.add(new Shower(
                R.string.geminids, GeocentricCoordinates.getInstance(112, 33),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 7),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 14),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 17),
                120));
        showers.add(new Shower(
                R.string.ursids, GeocentricCoordinates.getInstance(217, 76),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 17),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 23),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 26),
                10));
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
        for (Shower shower : showers) {
            sources.add(new MeteorRadiantSource(model, shower, getResources()));
        }
    }

    @Override
    public int getLayerDepthOrder() {
        return 80;
    }

    @Override
    public String getPreferenceId() {
        return "source_provider.6";
    }

    @Override
    public String getLayerName() {
        return "Meteor Showers";
    }

    @Override
    protected int getLayerNameId() {
        return R.string.show_meteors_pref;
    }

    /**
     * Represents a meteor shower.
     */
    private static class Shower {

        private final Calendar start;
        private final GeocentricCoordinates radiant;
        private final int nameId;
        private final Calendar peak;
        private final Calendar end;
        private final int peakMeteorsPerHour;

        public Shower(int nameId, GeocentricCoordinates radiant, Calendar start, Calendar peak, Calendar end, int peakMeteorsPerHour) {
            this.nameId = nameId;
            this.radiant = radiant;
            this.start = start;
            this.peak = peak;
            this.end = end;
            this.peakMeteorsPerHour = peakMeteorsPerHour;
        }
    }

    private static class MeteorRadiantSource extends AbstractAstronomicalSource {
        private static final int LABEL_COLOR = 0xf67e81;
        private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);
        private static final long UPDATE_FREQ_MS = TimeUtils.MILLISECONDS_PER_DAY;
        private static final float SCALE_FACTOR = 0.03f;

        private final List<ImageSource> imageSources = new ArrayList<>();
        private final List<TextSource> labelSources = new ArrayList<>();

        private final AstronomerModel model;
        private final ImageSourceImpl theImage;
        private final TextSource label;
        private final Shower shower;
        private final String name;
        private final List<String> searchNames = new ArrayList<>();
        private long lastUpdateTimeMs = 0L;

        public MeteorRadiantSource(AstronomerModel model, Shower shower, Resources resources) {
            this.model = model;
            this.shower = shower;
            this.name = resources.getString(shower.nameId);
            // Not sure what the right user experience should be here.  Should we only show up
            // in the search results when the shower is visible?  For now, just ensure
            // that it's obvious from the search label.
            CharSequence startDate = DateFormat.format("MMM dd", shower.start);
            CharSequence endDate = DateFormat.format("MMM dd", shower.end);
            searchNames.add(name + " (" + startDate + "-" + endDate + ")");
            // blank is a 1pxX1px image that should be invisible.
            // We'd prefer not to show any image except on the shower dates, but there
            // appears to be a bug in the renderer/layer interface in that Update values are not
            // respected.  Ditto the label.
            // TODO(johntaylor): fix the bug and remove this blank image
            theImage = new ImageSourceImpl(shower.radiant, resources, R.drawable.blank, UP, SCALE_FACTOR);
            imageSources.add(theImage);
            label = new TextSourceImpl(shower.radiant, name, LABEL_COLOR);
            labelSources.add(label);
        }

        @Override
        public List<String> getNames() {
            return searchNames;
        }

        @Override
        public GeocentricCoordinates getSearchLocation() {
            return shower.radiant;
        }

        private void updateShower() {
            lastUpdateTimeMs = model.getTimeMillis();
            // We will only show the shower if it's the right time of year.
            Calendar now = model.getTime();
            // Standardize on the same year as we stored for the showers.
            now.set(Calendar.YEAR, ANY_OLD_YEAR);

            theImage.setUpVector(UP);
            // TODO(johntaylor): consider varying the sizes by scaling factor as time progresses.
            if (now.after(shower.start) && now.before(shower.end)) {
                label.setText(name);
                double percentToPeak;
                if (now.before(shower.peak)) {
                    percentToPeak = (double) (now.getTimeInMillis() - shower.start.getTimeInMillis()) /
                            (shower.peak.getTimeInMillis() - shower.start.getTimeInMillis());
                } else {
                    percentToPeak = (double) (shower.end.getTimeInMillis() - now.getTimeInMillis()) /
                            (shower.end.getTimeInMillis() - shower.peak.getTimeInMillis());
                }
                // Not sure how best to calculate number of meteors - use linear interpolation for now.
                double numberOfMeteorsPerHour = shower.peakMeteorsPerHour * percentToPeak;
                if (numberOfMeteorsPerHour > METEOR_THRESHOLD_PER_HR) {
                    theImage.setImageId(R.drawable.meteors_1);
                } else {
                    theImage.setImageId(R.drawable.meteors_2);
                }
            } else {
                label.setText(" ");
                theImage.setImageId(R.drawable.blank);
            }
        }

        @Override
        public Sources initialize() {
            updateShower();
            return this;
        }

        @Override
        public EnumSet<UpdateType> update() {
            EnumSet<UpdateType> updateTypes = EnumSet.noneOf(UpdateType.class);
            if (Math.abs(model.getTime().getTimeInMillis() - lastUpdateTimeMs) > UPDATE_FREQ_MS) {
                updateShower();
                updateTypes.add(UpdateType.Reset);
            }
            return updateTypes;
        }

        @Override
        public List<? extends ImageSource> getImages() {
            return imageSources;
        }

        @Override
        public List<? extends TextSource> getLabels() {
            return labelSources;
        }
    }
}