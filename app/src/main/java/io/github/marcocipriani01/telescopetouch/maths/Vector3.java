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

package io.github.marcocipriani01.telescopetouch.maths;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Vector3 {

    public double x;
    public double y;
    public double z;

    public Vector3() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructs a Vector3 from a float[2] object.
     * Checks for length. This is probably inefficient, so if you're using this
     * you should already be questioning your use of float[] instead of Vector3.
     */
    public Vector3(float[] xyz) throws IllegalArgumentException {
        if (xyz.length != 3) {
            throw new IllegalArgumentException("Trying to create 3 vector from array of length: " + xyz.length);
        }
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }

    public static double scalarProduct(Vector3 v1, Vector3 v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static Vector3 vectorProduct(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.y * v2.z - v1.z * v2.y,
                -v1.x * v2.z + v1.z * v2.x,
                v1.x * v2.y - v1.y * v2.x);
    }

    public static double length(Vector3 v) {
        return Math.sqrt(lengthSqr(v));
    }

    public static double lengthSqr(Vector3 v) {
        return scalarProduct(v, v);
    }

    public static Vector3 normalized(Vector3 v) {
        double len = length(v);
        if (len < 0.000001)
            return new Vector3();
        return scale(v, 1.0 / len);
    }

    public static Vector3 projectOntoUnit(Vector3 v, Vector3 onto) {
        return scale(onto, scalarProduct(v, onto));
    }

    public static Vector3 negate(Vector3 v) {
        return new Vector3(-v.x, -v.y, -v.z);
    }

    public static Vector3 sum(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static Vector3 difference(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    /**
     * Scales the vector by the given amount and returns a new vector.
     */
    public static Vector3 scale(Vector3 v, double factor) {
        return new Vector3(v.x * factor, v.y * factor, v.z * factor);
    }

    public static double cosineSimilarity(Vector3 v1, Vector3 v2) {
        // We might want to optimize this implementation at some point.
        return (scalarProduct(v1, v2) / Math.sqrt(scalarProduct(v1, v1) * scalarProduct(v2, v2)));
    }

    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    /**
     * Assigns these values to the vector's components.
     */
    public void assign(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Assigns the values of the other vector to this one.
     */
    public void assign(Vector3 other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    /**
     * Returns the vector's length.
     */
    public float length() {
        return (float) Math.sqrt(length2());
    }

    /**
     * Returns the square of the vector's length.
     */
    public double length2() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    /**
     * Normalize the vector in place, i.e., map it to the corresponding unit vector.
     */
    public void normalize() {
        float norm = this.length();
        this.x = this.x / norm;
        this.y = this.y / norm;
        this.z = this.z / norm;
    }

    /**
     * Scale the vector in place.
     */
    public void scale(double scale) {
        this.x = this.x * scale;
        this.y = this.y * scale;
        this.z = this.z * scale;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Vector3)) return false;
        Vector3 other = (Vector3) object;
        // float equals is a bit of a dodgy concept
        return other.x == x && other.y == y && other.z == z;
    }

    @Override
    public int hashCode() {
        // This is dumb, but it will do for now.
        return Float.floatToIntBits((float) x) + Float.floatToIntBits((float) y) + Float.floatToIntBits((float) z);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("x=%f, y=%f, z=%f", x, y, z);
    }
}