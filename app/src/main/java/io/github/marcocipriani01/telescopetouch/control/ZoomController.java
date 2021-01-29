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

package io.github.marcocipriani01.telescopetouch.control;

/**
 * Controls the field of view of a user.
 *
 * @author John Taylor
 */
public class ZoomController extends AbstractController {

    private static final float MAX_ZOOM = 90.0f;
    private static final float MIN_ZOOM = 1.5f;

    @Override
    public void start() {
        // Nothing to do
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    /**
     * Increases the field of view by the given ratio.  That is, a number >1 will zoom the user
     * out, up to a predetermined maximum.
     */
    public void zoomBy(float ratio) {
        float zoomDegrees = model.getFieldOfView() * ratio;
        if (zoomDegrees > MAX_ZOOM) {
            zoomDegrees = MAX_ZOOM;
        } else {
            zoomDegrees = Math.max(zoomDegrees, MIN_ZOOM);
        }
        if (enabled)
            model.setFieldOfView(zoomDegrees);
    }
}