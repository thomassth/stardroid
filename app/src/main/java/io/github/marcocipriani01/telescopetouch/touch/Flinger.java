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

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Given a flung motion event, this class pumps new Motion events out
 * to simulate an underlying object with some inertia.
 */
public class Flinger {

    private static final String TAG = TelescopeTouchApp.getTag(Flinger.class);
    private final FlingListener listener;
    private final int UPDATES_PER_SECOND = 20;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> flingTask;

    public Flinger(FlingListener listener) {
        this.listener = listener;
        executor = Executors.newScheduledThreadPool(1);
    }

    public void fling(float velocityX, float velocityY) {
        Log.d(TAG, "Doing the fling");
        class PositionUpdater implements Runnable {
            private float myVelocityX, myVelocityY;

            public PositionUpdater(float velocityX, float velocityY) {
                this.myVelocityX = velocityX;
                this.myVelocityY = velocityY;
            }

            @Override
            public void run() {
                if (myVelocityX * myVelocityX + myVelocityY * myVelocityY < (float) 10) {
                    stop();
                }
                listener.fling(myVelocityX / UPDATES_PER_SECOND, myVelocityY / UPDATES_PER_SECOND);
                float decelFactor = 1.1f;
                myVelocityX /= decelFactor;
                myVelocityY /= decelFactor;
            }
        }
        int timeIntervalMillis = 1000 / UPDATES_PER_SECOND;
        flingTask = executor.scheduleAtFixedRate(new PositionUpdater(velocityX, velocityY),
                0, timeIntervalMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Brings the flinger to a dead stop.
     */
    public void stop() {
        if (flingTask != null) flingTask.cancel(true);
        Log.d(TAG, "Fling stopped");
    }

    public interface FlingListener {
        void fling(float distanceX, float distanceY);
    }
}