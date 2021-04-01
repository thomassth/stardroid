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