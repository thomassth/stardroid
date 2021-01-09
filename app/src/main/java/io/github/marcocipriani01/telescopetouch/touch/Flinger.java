package io.github.marcocipriani01.telescopetouch.touch;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Given a flung motion event, this class pumps new Motion events out
 * to simulate an underlying object with some inertia.
 */
public class Flinger {
    private static final String TAG = MiscUtil.getTag(Flinger.class);
    private final FlingListener listener;
    private final int updatesPerSecond = 20;
    private final int timeIntervalMillis = 1000 / updatesPerSecond;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> flingTask;

    public Flinger(FlingListener listener) {
        this.listener = listener;
        executor = Executors.newScheduledThreadPool(1);
    }

    public void fling(float velocityX, float velocityY) {
        Log.d(TAG, "Doing the fling");
        class PositionUpdater implements Runnable {
            private final float decelFactor = 1.1f;
            private final float TOL = 10;
            private float myVelocityX, myVelocityY;

            public PositionUpdater(float velocityX, float velocityY) {
                this.myVelocityX = velocityX;
                this.myVelocityY = velocityY;
            }

            public void run() {
                if (myVelocityX * myVelocityX + myVelocityY * myVelocityY < TOL) {
                    stop();
                }
                listener.fling(myVelocityX / updatesPerSecond,
                        myVelocityY / updatesPerSecond);
                myVelocityX /= decelFactor;
                myVelocityY /= decelFactor;
            }
        }
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
