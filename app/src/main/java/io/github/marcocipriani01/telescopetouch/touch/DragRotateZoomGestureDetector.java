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

package io.github.marcocipriani01.telescopetouch.touch;

import android.util.Log;
import android.view.MotionEvent;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Detects map drags, rotations and pinch zooms.
 *
 * @author John Taylor
 */
public class DragRotateZoomGestureDetector {

    private static final String TAG = TelescopeTouchApp.getTag(DragRotateZoomGestureDetector.class);
    private final DragRotateZoomGestureDetectorListener listener;
    private State currentState = State.READY;
    private float last1X;
    private float last1Y;
    private float last2X;
    private float last2Y;

    public DragRotateZoomGestureDetector(DragRotateZoomGestureDetectorListener listener) {
        this.listener = listener;
    }

    private static float normSquared(float x, float y) {
        return (x * x + y * y);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        // The state changes are as follows.
        // READY -> DRAGGING -> DRAGGING2 -> READY
        //
        // ACTION_DOWN: READY->DRAGGING
        //   last position = current position
        //
        // ACTION_MOVE: no state change
        //   calculate move = current position - last position
        //   last position = current position
        //
        // ACTION_UP: DRAGGING->READY
        //   last position = null
        // ...or...from DRAGGING
        //
        // ACTION_POINTER_DOWN: DRAGGING->DRAGGING2
        //   we're in multitouch mode
        //   last position1 = current position1
        //   last poisiton2 = current position2
        //
        // ACTION_MOVE:
        //   calculate move
        //   last position1 = current position1
        //   last position2 = current position2
        int actionCode = ev.getAction() & MotionEvent.ACTION_MASK;
        // Log.d(TAG, "Action: " + actionCode + ", current state " + currentState);
        if (actionCode == MotionEvent.ACTION_DOWN || currentState == State.READY) {
            currentState = State.DRAGGING;
            last1X = ev.getX();
            last1Y = ev.getY();
            // Log.d(TAG, "Down.  Store last position " + last1X + ", " + last1Y);
            return true;
        }

        if (actionCode == MotionEvent.ACTION_MOVE && currentState == State.DRAGGING) {
            // Log.d(TAG, "Move");
            float current1X = ev.getX();
            float current1Y = ev.getY();
            // Log.d(TAG, "Move.  Last position " + last1X + ", " + last1Y +
            //    "Current position " + current1X + ", " + current1Y);
            listener.onDrag(current1X - last1X, current1Y - last1Y);
            last1X = current1X;
            last1Y = current1Y;
            return true;
        }

        if (actionCode == MotionEvent.ACTION_MOVE && currentState == State.DRAGGING2) {
            // Log.d(TAG, "Move with two fingers");
            int pointerCount = ev.getPointerCount();
            if (pointerCount != 2) {
                Log.w(TAG, "Expected exactly two pointers but got " + pointerCount);
                return false;
            }
            float current1X = ev.getX(0);
            float current1Y = ev.getY(0);
            float current2X = ev.getX(1);
            float current2Y = ev.getY(1);
            // Log.d(TAG, "Old Point 1: " + lastPointer1X + ", " + lastPointer1Y);
            // Log.d(TAG, "Old Point 2: " + lastPointer2X + ", " + lastPointer2Y);
            // Log.d(TAG, "New Point 1: " + current1X + ", " + current1Y);
            // Log.d(TAG, "New Point 2: " + current2X + ", " + current2Y);

            float distanceMovedX1 = current1X - last1X;
            float distanceMovedY1 = current1Y - last1Y;
            float distanceMovedX2 = current2X - last2X;
            float distanceMovedY2 = current2Y - last2Y;

            // Log.d(TAG, "Point 1 moved by: " + distanceMovedX1 + ", " + distanceMovedY1);
            // Log.d(TAG, "Point 2 moved by: " + distanceMovedX2 + ", " + distanceMovedY2);

            // Dragging map by the mean of the points
            listener.onDrag((distanceMovedX1 + distanceMovedX2) / 2,
                    (distanceMovedY1 + distanceMovedY2) / 2);

            // These are the vectors between the two points.
            float vectorLastX = last1X - last2X;
            float vectorLastY = last1Y - last2Y;
            float vectorCurrentX = current1X - current2X;
            float vectorCurrentY = current1Y - current2Y;

            // Log.d(TAG, "Previous vector: " + vectorBeforeX + ", " + vectorBeforeY);
            // Log.d(TAG, "Current vector: " + vectorCurrentX + ", " + vectorCurrentY);

            float lengthRatio = (float) Math.sqrt(normSquared(vectorCurrentX, vectorCurrentY) / normSquared(vectorLastX, vectorLastY));
            // Log.d(TAG, "Stretching map by ratio " + ratio);
            listener.onStretch(lengthRatio);
            float angleLast = (float) Math.atan2(vectorLastX, vectorLastY);
            float angleCurrent = (float) Math.atan2(vectorCurrentX, vectorCurrentY);
            // Log.d(TAG, "Angle before " + angleBefore);
            // Log.d(TAG, "Angle after " + angleAfter);
            float angleDelta = angleCurrent - angleLast;
            // Log.d(TAG, "Rotating map by angle delta " + angleDelta);
            listener.onRotate(angleDelta * (180 / (float) Math.PI));

            last1X = current1X;
            last1Y = current1Y;
            last2X = current2X;
            last2Y = current2Y;
            return true;
        }

        if (actionCode == MotionEvent.ACTION_UP) {
            // Log.d(TAG, "Up");
            currentState = State.READY;
            return true;
        }

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN && currentState == State.DRAGGING) {
            //Log.d(TAG, "Non primary pointer down " + pointer);
            int pointerCount = ev.getPointerCount();
            if (pointerCount != 2) {
                Log.w(TAG, "Expected exactly two pointers but got " + pointerCount);
                return false;
            }
            currentState = State.DRAGGING2;
            last1X = ev.getX(0);
            last1Y = ev.getY(0);
            last2X = ev.getX(1);
            last2Y = ev.getY(1);
            return true;
        }

        if (actionCode == MotionEvent.ACTION_POINTER_UP && currentState == State.DRAGGING2) {
            // Log.d(TAG, "Non primary pointer up " + pointer);
            // Let's just drop dragging for now - can worry about continuity with one finger
            // drag later.
            currentState = State.READY;
            return true;
        }
        // Log.d(TAG, "End state " + currentState);
        return false;
    }

    private enum State {READY, DRAGGING, DRAGGING2}

    /**
     * Listens for the gestures detected by the {@link DragRotateZoomGestureDetector}.
     *
     * @author John Taylor
     */
    public interface DragRotateZoomGestureDetectorListener {
        void onDrag(float xPixels, float yPixels);

        void onStretch(float ratio);

        void onRotate(float radians);
    }
}