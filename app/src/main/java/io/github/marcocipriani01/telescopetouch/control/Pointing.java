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

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

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
        return (float) lineOfSight.x;
    }

    public float getLineOfSightY() {
        return (float) lineOfSight.y;
    }

    public float getLineOfSightZ() {
        return (float) lineOfSight.z;
    }

    public float getPerpendicularX() {
        return (float) perpendicular.x;
    }

    public float getPerpendicularY() {
        return (float) perpendicular.y;
    }

    public float getPerpendicularZ() {
        return (float) perpendicular.z;
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