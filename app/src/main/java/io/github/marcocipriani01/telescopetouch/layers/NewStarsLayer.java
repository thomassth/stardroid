package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.AssetManager;
import android.content.res.Resources;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * An implementation of the {@link AbstractFileBasedLayer} for displaying stars
 * in the Renderer.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class NewStarsLayer extends AbstractFileBasedLayer {

    public NewStarsLayer(AssetManager assetManager, Resources resources) {
        super(assetManager, resources, "stars.binary");
    }

    @Override
    public int getLayerDepthOrder() {
        return 30;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.stars;
    }

    // TODO(brent): Remove this.
    @Override
    public String getPreferenceId() {
        return "source_provider.0";
    }
}