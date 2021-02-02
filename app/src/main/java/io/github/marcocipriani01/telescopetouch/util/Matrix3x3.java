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

package io.github.marcocipriani01.telescopetouch.util;

import androidx.annotation.NonNull;

/**
 * Class for representing a 3x3 matrix explicitly, avoiding heap
 * allocation as far as possible.
 *
 * @author Dominic Widdows
 */
public class Matrix3x3 implements Cloneable {

    public float xx;
    public float xy;
    public float xz;
    public float yx;
    public float yy;
    public float yz;
    public float zx;
    public float zy;
    public float zz;

    /**
     * Construct a new matrix.
     *
     * @param xx row 1, col 1
     * @param xy row 1, col 2
     * @param xz row 1, col 3
     * @param yx row 2, col 1
     * @param yy row 2, col 2
     * @param yz row 2, col 3
     * @param zx row 3, col 1
     * @param zy row 3, col 2
     * @param zz row 3, col 3
     */
    public Matrix3x3(float xx, float xy, float xz,
                     float yx, float yy, float yz,
                     float zx, float zy, float zz) {
        this.xx = xx;
        this.xy = xy;
        this.xz = xz;
        this.yx = yx;
        this.yy = yy;
        this.yz = yz;
        this.zx = zx;
        this.zy = zy;
        this.zz = zz;
    }

    /**
     * Construct a matrix from three column vectors.
     */
    public Matrix3x3(Vector3 v1, Vector3 v2, Vector3 v3) {
        this(v1, v2, v3, true);
    }

    /**
     * Construct a matrix from three vectors.
     *
     * @param columnVectors true if the vectors are column vectors, otherwise
     *                      they're row vectors.
     */
    public Matrix3x3(Vector3 v1, Vector3 v2, Vector3 v3, boolean columnVectors) {
        if (columnVectors) {
            this.xx = v1.x;
            this.yx = v1.y;
            this.zx = v1.z;
            this.xy = v2.x;
            this.yy = v2.y;
            this.zy = v2.z;
            this.xz = v3.x;
            this.yz = v3.y;
        } else {
            this.xx = v1.x;
            this.xy = v1.y;
            this.xz = v1.z;
            this.yx = v2.x;
            this.yy = v2.y;
            this.yz = v2.z;
            this.zx = v3.x;
            this.zy = v3.y;
        }
        this.zz = v3.z;
    }

    /**
     * Create a zero matrix.
     */
    public Matrix3x3() {
        new Matrix3x3(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public static Matrix3x3 getIdMatrix() {
        return new Matrix3x3(1, 0, 0, 0, 1, 0, 0, 0, 1);
    }

    /**
     * Multiply two 3X3 matrices m1 * m2.
     */
    public static Matrix3x3 matrixMultiply(Matrix3x3 m1, Matrix3x3 m2) {
        return new Matrix3x3(m1.xx * m2.xx + m1.xy * m2.yx + m1.xz * m2.zx,
                m1.xx * m2.xy + m1.xy * m2.yy + m1.xz * m2.zy,
                m1.xx * m2.xz + m1.xy * m2.yz + m1.xz * m2.zz,
                m1.yx * m2.xx + m1.yy * m2.yx + m1.yz * m2.zx,
                m1.yx * m2.xy + m1.yy * m2.yy + m1.yz * m2.zy,
                m1.yx * m2.xz + m1.yy * m2.yz + m1.yz * m2.zz,
                m1.zx * m2.xx + m1.zy * m2.yx + m1.zz * m2.zx,
                m1.zx * m2.xy + m1.zy * m2.yy + m1.zz * m2.zy,
                m1.zx * m2.xz + m1.zy * m2.yz + m1.zz * m2.zz);
    }

    /**
     * Calculate w = m * v where m is a 3X3 matrix and v a column vector.
     */
    public static Vector3 matrixVectorMultiply(Matrix3x3 m, Vector3 v) {
        return new Vector3(m.xx * v.x + m.xy * v.y + m.xz * v.z,
                m.yx * v.x + m.yy * v.y + m.yz * v.z,
                m.zx * v.x + m.zy * v.y + m.zz * v.z);
    }

    /**
     * Calculate the rotation matrix for a certain number of degrees about the
     * give axis.
     *
     * @param axis - must be a unit vector.
     */
    public static Matrix3x3 calculateRotationMatrix(float degrees, Vector3 axis) {
        // Construct the rotation matrix about this vector
        float cosD = (float) Math.cos(degrees * MathsUtils.DEGREES_TO_RADIANS);
        float sinD = (float) Math.sin(degrees * MathsUtils.DEGREES_TO_RADIANS);
        float oneMinusCosD = 1f - cosD;

        float x = axis.x;
        float y = axis.y;
        float z = axis.z;

        float xs = x * sinD;
        float ys = y * sinD;
        float zs = z * sinD;

        float xm = x * oneMinusCosD;
        float ym = y * oneMinusCosD;
        float zm = z * oneMinusCosD;

        float xym = x * ym;
        float yzm = y * zm;
        float zxm = z * xm;

        return new Matrix3x3(x * xm + cosD, xym + zs, zxm - ys,
                xym - zs, y * ym + cosD, yzm + xs,
                zxm + ys, yzm - xs, z * zm + cosD);
    }

    @NonNull
    public Matrix3x3 copy() {
        return new Matrix3x3(xx, xy, xz,
                yx, yy, yz,
                zx, zy, zz);
    }

    public float getDeterminant() {
        return xx * yy * zz + xy * yz * zx + xz * yx * zy - xx * yz * zy - yy * zx * xz - zz * xy * yx;
    }

    public Matrix3x3 getInverse() {
        float det = getDeterminant();
        if (det == 0.0) return null;
        return new Matrix3x3(
                (yy * zz - yz * zy) / det, (xz * zy - xy * zz) / det, (xy * yz - xz * yy) / det,
                (yz * zx - yx * zz) / det, (xx * zz - xz * zx) / det, (xz * yx - xx * yz) / det,
                (yx * zy - yy * zx) / det, (xy * zx - xx * zy) / det, (xx * yy - xy * yx) / det);
    }

    /**
     * Transpose the matrix, in place.
     */
    public void transpose() {
        float tmp;
        tmp = xy;
        xy = yx;
        yx = tmp;

        tmp = xz;
        xz = zx;
        zx = tmp;

        tmp = yz;
        yz = zy;
        zy = tmp;
    }
}