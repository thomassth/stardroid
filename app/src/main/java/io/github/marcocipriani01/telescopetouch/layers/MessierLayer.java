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
public class MessierLayer extends AbstractFileBasedLayer {

    public static final int DEPTH_ORDER = 10;
    public static final String PREFERENCE_ID = "source_provider.2";

    public MessierLayer(AssetManager assetManager, Resources resources) {
        super(assetManager, resources, "messier.binary");
    }

    @Override
    public int getLayerDepthOrder() {
        return DEPTH_ORDER;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.messier_objects;
    }

    @Override
    public String getPreferenceId() {
        return PREFERENCE_ID;
    }
}