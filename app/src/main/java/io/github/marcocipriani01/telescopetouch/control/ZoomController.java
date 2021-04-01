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