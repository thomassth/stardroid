package io.github.marcocipriani01.telescopetouch.layers;

import android.content.SharedPreferences;
import android.content.res.Resources;

import java.util.ArrayList;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.provider.ephemeris.Planet;
import io.github.marcocipriani01.telescopetouch.provider.ephemeris.PlanetSource;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;

/**
 * An implementation of the {@link Layer} interface for displaying planets in
 * the Renderer.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
public class PlanetsLayer extends AbstractSourceLayer {
    private final SharedPreferences preferences;
    private final AstronomerModel model;

    public PlanetsLayer(AstronomerModel model, Resources resources, SharedPreferences preferences) {
        super(resources, true);
        this.preferences = preferences;
        this.model = model;
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
        for (Planet planet : Planet.values()) {
            sources.add(new PlanetSource(planet, getResources(), model, preferences));
        }
    }

    // If the preference Id is needed. There is no super method and no need
    // to override.
    public String getPreferenceId() {
        return "source_provider.3";
    }

    @Override
    public int getLayerDepthOrder() {
        // TODO(brent): refactor these to a common location.
        return 60;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.solar_system;
    }
}
