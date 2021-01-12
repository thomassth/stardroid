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

package io.github.marcocipriani01.telescopetouch.source;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * Base implementation of the {@link AstronomicalSource} and {@link Sources}
 * interfaces.
 *
 * @author Brent Bryan
 */
public abstract class AbstractAstronomicalSource implements AstronomicalSource, Sources {

    @Override
    public Sources initialize() {
        return this;
    }

    @Override
    public EnumSet<UpdateType> update() {
        return EnumSet.noneOf(UpdateType.class);
    }

    /**
     * Implementors of this method must implement {@link #getSearchLocation}.
     */
    @Override
    public List<String> getNames() {
        return Collections.emptyList();
    }

    @Override
    public GeocentricCoordinates getSearchLocation() {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public List<? extends ImageSource> getImages() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends TextSource> getLabels() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends LineSource> getLines() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends PointSource> getPoints() {
        return Collections.emptyList();
    }
}
