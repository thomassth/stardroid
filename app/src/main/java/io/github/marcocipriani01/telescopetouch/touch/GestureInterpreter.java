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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.github.marcocipriani01.telescopetouch.activities.util.FullscreenControlsManager;

/**
 * Processes touch events and scrolls the screen in manual mode.
 *
 * @author John Taylor
 * @author marcocipriani01
 */
public class GestureInterpreter extends GestureDetector.SimpleOnGestureListener {

    private static final float DOUBLE_TAP_ZOOM = 1.5f / 2f;
    private final FullscreenControlsManager fullscreenControlsManager;
    private final MapMover mapMover;
    private final ScheduledExecutorService executor;
    private int updateRate = 30;
    private ScheduledFuture<?> zoomTask;
    private ScheduledFuture<?> flingTask;

    public GestureInterpreter(FullscreenControlsManager fullscreenControlsManager, MapMover mapMover) {
        this.fullscreenControlsManager = fullscreenControlsManager;
        this.mapMover = mapMover;
        executor = Executors.newScheduledThreadPool(1);
    }

    public void setUpdateRate(int updateRate) {
        this.updateRate = updateRate;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        stopFling();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        flingTask = executor.scheduleAtFixedRate(new PositionUpdater(velocityX, velocityY), 0,
                1000 / updateRate, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        zoomTask = executor.scheduleAtFixedRate(new ZoomUpdater(), 0,
                1000 / updateRate, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        fullscreenControlsManager.toggleControls();
        return true;
    }

    public void stopZoom() {
        if (zoomTask != null) zoomTask.cancel(true);
    }

    public void stopFling() {
        if (flingTask != null) flingTask.cancel(true);
    }

    private class ZoomUpdater implements Runnable {

        private float currentStretch = 0f;

        @Override
        public void run() {
            float diff = (currentStretch - DOUBLE_TAP_ZOOM),
                    factor = (-0.45f * diff * diff + 0.3f) / updateRate * 30f;
            currentStretch += factor;
            mapMover.onStretch(1f + factor);
            if (currentStretch >= 1.5f)
                stopZoom();
        }
    }

    private class PositionUpdater implements Runnable {

        private float velocityX, velocityY;

        public PositionUpdater(float velocityX, float velocityY) {
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        @Override
        public void run() {
            if (((velocityX * velocityX) + (velocityY * velocityY)) < 10.0f)
                stopFling();
            mapMover.onDrag(velocityX / updateRate, velocityY / updateRate);
            final float decelerationFactor = 1.1f;
            velocityX /= decelerationFactor;
            velocityY /= decelerationFactor;
        }
    }
}