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

package io.github.marcocipriani01.telescopetouch.renderer.util;

import io.github.marcocipriani01.telescopetouch.maths.Matrix4x4;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

public class SearchHelper {

    private Vector3 mTarget = new Vector3();
    private Vector3 mTransformedPosition = new Vector3();
    private float mHalfScreenWidth = 1;
    private float mHalfScreenHeight = 1;
    private Matrix4x4 mTransformMatrix = null;
    private float mTargetFocusRadius = 0;
    private float mTransitionFactor = 0;
    private long mLastUpdateTime = 0;
    private boolean mWasInFocusLastCheck = false;
    private String mTargetName = "Default target name";

    public void resize(int width, int height) {
        mHalfScreenWidth = width * 0.5f;
        mHalfScreenHeight = height * 0.5f;
    }

    public void setTarget(Vector3 target, String targetName) {
        mTargetName = targetName;
        mTarget = target.copy();
        mTransformedPosition = null;
        mLastUpdateTime = System.currentTimeMillis();
        mTransitionFactor = targetInFocusRadiusImpl() ? 1 : 0;
    }

    public void setTransform(Matrix4x4 transformMatrix) {
        mTransformMatrix = transformMatrix;
        mTransformedPosition = null;
    }

    public Vector3 getTransformedPosition() {
        if (mTransformedPosition == null && mTransformMatrix != null) {
            // Transform the label position by our transform matrix
            mTransformedPosition = Matrix4x4.transformVector(mTransformMatrix, mTarget);
        }
        return mTransformedPosition;
    }

    public boolean targetInFocusRadius() {
        return mWasInFocusLastCheck;
    }

    public void setTargetFocusRadius(float radius) {
        mTargetFocusRadius = radius;
    }

    // Returns a number between 0 and 1, 0 meaning that we should draw the UI as if the target
    // is not in focus, 1 meaning it should be fully in focus, and between the two meaning
    // it just transitioned between the two, so we should be drawing the transition.
    public float getTransitionFactor() {
        return mTransitionFactor;
    }

    // Checks whether the search target is in the focus or not, and updates the seconds in the state
    // accordingly.
    public void checkState() {
        boolean inFocus = targetInFocusRadiusImpl();
        mWasInFocusLastCheck = inFocus;
        long time = System.currentTimeMillis();
        float delta = 0.001f * (time - mLastUpdateTime);
        mTransitionFactor += delta * (inFocus ? 1 : -1);
        mTransitionFactor = Math.min(1, Math.max(0, mTransitionFactor));
        mLastUpdateTime = time;
    }

    public String getTargetName() {
        return mTargetName;
    }

    // Returns the distance from the center of the screen, in pixels, if the target is in front of
    // the viewer.  Returns infinity if the point is behind the viewer.
    private float getDistanceFromCenterOfScreen() {
        Vector3 position = getTransformedPosition();
        if (position.z > 0) {
            float dx = (float) position.x * mHalfScreenWidth;
            float dy = (float) position.y * mHalfScreenHeight;
            return (float) Math.sqrt(dx * dx + dy * dy);
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    private boolean targetInFocusRadiusImpl() {
        float distFromCenter = getDistanceFromCenterOfScreen();
        return 0.5f * mTargetFocusRadius > distFromCenter;
    }
}