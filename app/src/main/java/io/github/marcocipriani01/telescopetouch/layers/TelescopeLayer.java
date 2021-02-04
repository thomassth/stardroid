package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.Sources;
import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.source.impl.ImageSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.TextSourceImpl;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class TelescopeLayer extends AbstractLayer {

    public static final int DEPTH_ORDER = 100;
    public static final String PREFERENCE_ID = "source_provider.7";
    private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);
    private static final int LABEL_COLOR = 0xff6f00;
    private static final float SIZE_ON_MAP = 0.035f;

    public TelescopeLayer(Resources resources) {
        super(resources, true);
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
        sources.add(new TelescopeSource());
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
        return R.string.telescope;
    }

    private class TelescopeSource extends AstronomicalSource {

        private final ArrayList<ImageSourceImpl> imageSources = new ArrayList<>();
        private final ArrayList<TextSource> labelSources = new ArrayList<>();
        private long lastUpdateTimeMs = 0L;
        private TextSourceImpl textSource;
        private ImageSourceImpl imageSource;

        @Override
        public Sources initialize() {
            Resources resources = getResources();
            imageSource = new ImageSourceImpl(connectionManager.telescopeCoordinates, resources, R.drawable.telescope_crosshair, UP, SIZE_ON_MAP);
            imageSources.add(imageSource);
            textSource = new TextSourceImpl(connectionManager.telescopeCoordinates, getName(), LABEL_COLOR);
            labelSources.add(textSource);
            return this;
        }

        @Override
        public EnumSet<RendererObjectManager.UpdateType> update() {
            EnumSet<RendererObjectManager.UpdateType> updates = EnumSet.noneOf(RendererObjectManager.UpdateType.class);
            long time = System.currentTimeMillis();
            if (Math.abs(time - lastUpdateTimeMs) > 200) {
                lastUpdateTimeMs = time;
                updates.add(RendererObjectManager.UpdateType.UpdatePositions);
                textSource.setText(getName());
                imageSource.setUpVector(UP);
            }
            return updates;
        }

        @Override
        public List<String> getNames() {
            return Collections.singletonList(getName());
        }

        private String getName() {
            return (connectionManager.telescopeName == null) ? "No telescope" : connectionManager.telescopeName;
        }

        @Override
        public GeocentricCoordinates getSearchLocation() {
            return connectionManager.telescopeCoordinates;
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