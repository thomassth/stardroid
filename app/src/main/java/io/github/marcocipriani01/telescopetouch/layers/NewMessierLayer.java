package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.AssetManager;
import android.content.res.Resources;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * An implementation of the {@link AbstractFileBasedLayer} for displaying
 * Messier objects.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class NewMessierLayer extends AbstractFileBasedLayer {
    public NewMessierLayer(AssetManager assetManager, Resources resources) {
        super(assetManager, resources, "messier.binary");
    }

    @Override
    public int getLayerDepthOrder() {
        return 20;
    }

    @Override
    protected int getLayerNameId() {
        // TODO(johntaylor): rename this string id
        return R.string.show_messier_objects_pref;
    }

    // TODO(brent): Remove this.
    @Override
    public String getPreferenceId() {
        return "source_provider.2";
    }
}
