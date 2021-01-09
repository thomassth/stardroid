package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.AssetManager;
import android.content.res.Resources;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * An implementation of the {@link AbstractFileBasedLayer} to display
 * Constellations in the renderer.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class NewConstellationsLayer extends AbstractFileBasedLayer {
    public NewConstellationsLayer(AssetManager assetManager, Resources resources) {
        super(assetManager, resources, "constellations.binary");
    }

    @Override
    public int getLayerDepthOrder() {
        return 10;
    }

    @Override
    public int getLayerNameId() {
        // TODO(johntaylor): rename this string id.
        return R.string.show_constellations_pref;
    }

    // TODO(brent): Remove this.
    @Override
    public String getPreferenceId() {
        return "source_provider.1";
    }
}
