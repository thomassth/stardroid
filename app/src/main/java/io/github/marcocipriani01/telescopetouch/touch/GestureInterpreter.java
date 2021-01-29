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

package io.github.marcocipriani01.telescopetouch.touch;

import android.view.GestureDetector;
import android.view.MotionEvent;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.FullscreenControlsManager;

/**
 * Processes touch events and scrolls the screen in manual mode.
 *
 * @author John Taylor
 */
public class GestureInterpreter extends GestureDetector.SimpleOnGestureListener {

    private final FullscreenControlsManager fullscreenControlsManager;
    private final MapMover mapMover;
    private final Flinger flinger = new Flinger(new Flinger.FlingListener() {
        public void fling(float distanceX, float distanceY) {
            mapMover.onDrag(distanceX, distanceY);
        }
    });

    public GestureInterpreter(FullscreenControlsManager fullscreenControlsManager, MapMover mapMover) {
        this.fullscreenControlsManager = fullscreenControlsManager;
        this.mapMover = mapMover;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //Log.d(TAG, "Tap down");
        flinger.stop();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Log.d(TAG, "Flinging " + velocityX + ", " + velocityY);
        flinger.fling(velocityX, velocityY);
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //Log.d(TAG, "Tap up");
        fullscreenControlsManager.toggleControls();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //Log.d(TAG, "Double tap");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        //Log.d(TAG, "Confirmed single tap");
        return false;
    }
}