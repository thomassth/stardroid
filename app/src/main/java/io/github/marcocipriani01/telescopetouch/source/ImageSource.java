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

package io.github.marcocipriani01.telescopetouch.source;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

/**
 * A celestial object represented by an image, such as a planet or a
 * galaxy.
 */
public class ImageSource extends AbstractSource implements PositionSource {

    public static final Vector3 UP = new Vector3(0, 1, 0);
    private final float imageScale;
    private final Resources resources;
    // These two vectors, along with Source.xyz, determine the position of the
    // image object.  The corners are as follows
    //
    //  xyz-u+v   xyz+u+v
    //     +---------+     ^
    //     |   xyz   |     | v
    //     |    .    |     .
    //     |         |
    //     +---------+
    //  xyz-u-v    xyz+u-v
    //
    //          .--->
    //            u
    public float ux, uy, uz;
    public float vx, vy, vz;
    public Bitmap image;

    public ImageSource(GeocentricCoordinates coords, Resources res, int id, float imageScale) {
        this(coords, res, id, UP, imageScale);
    }

    public ImageSource(GeocentricCoordinates coords, Resources res, int id, Vector3 upVec, float imageScale) {
        super(coords, Color.WHITE);
        this.imageScale = imageScale;
        // TODO(jpowell): We're never freeing this resource, so we leak it every
        // time we create a new ImageSourceImpl and garbage collect an old one.
        // We need to make sure it gets freed.
        // We should also cache this so we don't have to keep reloading these
        // which is really slow and adds noticeable lag to the application when it
        // happens.
        this.resources = res;
        setUpVector(upVec);
        setImageId(id);
    }

    public void setImageId(int imageId) {
        Options opts = new Options();
        opts.inScaled = false;
        this.image = BitmapFactory.decodeResource(resources, imageId, opts);
        if (image == null) {
            throw new RuntimeException("Coud not decode image " + imageId);
        }
    }

    /**
     * Returns the image to be displayed at the specified point.
     */
    public Bitmap getImage() {
        return image;
    }

    public float[] getHorizontalCorner() {
        return new float[]{ux, uy, uz};
    }

    public float[] getVerticalCorner() {
        return new float[]{vx, vy, vz};
    }

    protected Resources getResources() {
        return resources;
    }

    public void setUpVector(Vector3 upVec) {
        Vector3 p = this.getLocation();
        Vector3 u = Vector3.negate(Vector3.normalized(Vector3.vectorProduct(p, upVec)));
        Vector3 v = Vector3.vectorProduct(u, p);
        v.scale(imageScale);
        u.scale(imageScale);
        // TODO(serafini): Can we replace these with a float[]?
        ux = (float) u.x;
        uy = (float) u.y;
        uz = (float) u.z;
        vx = (float) v.x;
        vy = (float) v.y;
        vz = (float) v.z;
    }

    public void resetUpVector() {
        setUpVector(UP);
    }
}