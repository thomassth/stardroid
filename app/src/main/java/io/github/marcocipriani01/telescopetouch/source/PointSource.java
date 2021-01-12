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

/**
 * This interface corresponds to an object which can be represented by a single
 * point in space, such as a star.
 *
 * @author Brent Bryan
 */
public interface PointSource extends Colorable, PositionSource {
    /**
     * Returns the size of the dot which should be drawn to represent this point
     * in the renderer.
     */
    int getSize();

    /**
     * Returns the Shape of the image used to render the point in the texture file.
     */
    Shape getPointShape();

    enum Shape {
        CIRCLE(),
        STAR(),
        ELLIPTICAL_GALAXY(),
        SPIRAL_GALAXY(),
        IRREGULAR_GALAXY(),
        LENTICULAR_GALAXY(),
        GLOBULAR_CLUSTER(),
        OPEN_CLUSTER(),
        NEBULA(),
        HUBBLE_DEEP_FIELD();

        public int getImageIndex() {
            // return imageIndex;
            return 0;
        }
    }
}