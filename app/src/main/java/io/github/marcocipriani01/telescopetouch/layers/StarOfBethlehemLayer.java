package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.base.TimeConstants;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.source.AbstractAstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.Sources;
import io.github.marcocipriani01.telescopetouch.source.impl.ImageSourceImpl;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.Vector3;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * A {@link Layer} specially for Christmas.
 *
 * @author John Taylor
 */

public class StarOfBethlehemLayer extends AbstractSourceLayer {
    public static final String TAG = MiscUtil.getTag(StarOfBethlehemLayer.class);
    private final AstronomerModel model;

    public StarOfBethlehemLayer(AstronomerModel model, Resources resources) {
        super(resources, true);
        this.model = model;
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
        sources.add(new StarOfBethlehemSource(model, getResources()));
    }

    @Override
    public int getLayerDepthOrder() {
        return 40;
    }

    // TODO(brent): Remove this.
    @Override
    public String getPreferenceId() {
        return "source_provider.0";
    }

    @Override
    public String getLayerName() {
        return "Easter Egg";
    }

    @Override
    protected int getLayerNameId() {
        return R.string.show_stars_pref;
    }

    private static class StarOfBethlehemSource extends AbstractAstronomicalSource {
        private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);
        private static final long UPDATE_FREQ_MS = 1L * TimeConstants.MILLISECONDS_PER_MINUTE;
        private static final float SCALE_FACTOR = 0.03f;

        private final List<ImageSource> imageSources = new ArrayList<ImageSource>();
        private final AstronomerModel model;
        private final GeocentricCoordinates coords;
        private final ImageSourceImpl theImage;
        private long lastUpdateTimeMs = 0L;

        public StarOfBethlehemSource(AstronomerModel model, Resources resources) {
            this.model = model;
            coords = new GeocentricCoordinates(1, 0, 0);
            // star_off2 is a 1pxX1px image that should be invisible.
            // We'd prefer not to show any image except on the Christmas dates, but there
            // appears to be a bug in the renderer in that new images added later don't get
            // picked up, even if we return UpdateType.Reset.
            theImage = new ImageSourceImpl(coords, resources, R.drawable.blank, UP, SCALE_FACTOR);
            imageSources.add(theImage);
        }

        private void updateStar() {
            this.lastUpdateTimeMs = model.getTime().getTime();
            // We will only show the star if it's Christmas Eve.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(model.getTime().getTime());
            theImage.setUpVector(UP);
            // TODO(johntaylor): consider varying the sizes by scaling factor as time progresses.
            if ((calendar.get(Calendar.MONTH) == Calendar.DECEMBER)
                    && (calendar.get(Calendar.DAY_OF_MONTH) == 25)) {
                Log.d(TAG, "Showing Easter Egg");
                theImage.setImageId(R.drawable.star_of_b);
                GeocentricCoordinates zenith = model.getZenith();
                GeocentricCoordinates east = model.getEast();
                coords.assign((zenith.x + 2 * east.x) / 3,
                        (zenith.y + 2 * east.y) / 3,
                        (zenith.z + 2 * east.z) / 3);
                theImage.setUpVector(zenith);
            } else {
                theImage.setImageId(R.drawable.blank);
            }
        }

        @Override
        public Sources initialize() {
            updateStar();
            return this;
        }

        @Override
        public EnumSet<UpdateType> update() {
            EnumSet<UpdateType> updateTypes = EnumSet.noneOf(UpdateType.class);
            if (Math.abs(model.getTime().getTime() - lastUpdateTimeMs) > UPDATE_FREQ_MS) {
                updateStar();
                updateTypes.add(UpdateType.UpdateImages);
                updateTypes.add(UpdateType.UpdatePositions);
            }
            return updateTypes;
        }

        @Override
        public List<? extends ImageSource> getImages() {
            return imageSources;
        }
    }
}
