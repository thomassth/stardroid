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

import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.util.Vector3;

/**
 * A POJO to hold the user's view direction.
 *
 * @author John Taylor
 */
public class Pointing {

    private final GeocentricCoordinates lineOfSight;
    private final GeocentricCoordinates perpendicular;

    public Pointing(GeocentricCoordinates lineOfSight, GeocentricCoordinates perpendicular) {
        this.lineOfSight = lineOfSight.copy();
        this.perpendicular = perpendicular.copy();
    }

    public Pointing() {
        this(new GeocentricCoordinates(1, 0, 0), new GeocentricCoordinates(0, 1, 0));
    }

    /**
     * Gets the line of sight component of the pointing.
     * Warning: creates a copy - if you can reuse your own
     * GeocentricCoordinates object it might be more efficient to
     * use {@link #getLineOfSightX()} etc.
     */
    public GeocentricCoordinates getLineOfSight() {
        return lineOfSight.copy();
    }

    /**
     * Gets the perpendicular component of the pointing.
     * Warning: creates a copy - if you can reuse your own
     * GeocentricCoordinates object it might be more efficient to
     * use {@link #getLineOfSightX()} etc.
     */
    public GeocentricCoordinates getPerpendicular() {
        return perpendicular.copy();
    }

    public float getLineOfSightX() {
        return lineOfSight.x;
    }

    public float getLineOfSightY() {
        return lineOfSight.y;
    }

    public float getLineOfSightZ() {
        return lineOfSight.z;
    }

    public float getPerpendicularX() {
        return perpendicular.x;
    }

    public float getPerpendicularY() {
        return perpendicular.y;
    }

    public float getPerpendicularZ() {
        return perpendicular.z;
    }

    /**
     * Only the AstronomerModel should change this.
     */
    void updatePerpendicular(Vector3 newPerpendicular) {
        perpendicular.assign(newPerpendicular);
    }

    /**
     * Only the AstronomerModel should change this.
     */
    void updateLineOfSight(Vector3 newLineOfSight) {
        lineOfSight.assign(newLineOfSight);
    }
}