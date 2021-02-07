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
    public boolean requiresBlending = false;

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

    public boolean requiresBlending() {
        return requiresBlending;
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