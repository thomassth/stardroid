package io.github.marcocipriani01.telescopetouch.touch;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import io.github.marcocipriani01.telescopetouch.activities.util.FullscreenControlsManager;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Processes touch events and scrolls the screen in manual mode.
 *
 * @author John Taylor
 */
public class GestureInterpreter extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = MiscUtil.getTag(GestureInterpreter.class);
    private final FullscreenControlsManager fullscreenControlsManager;
    private final MapMover mapMover;
    private final Flinger flinger = new Flinger(new Flinger.FlingListener() {
        public void fling(float distanceX, float distanceY) {
            mapMover.onDrag(distanceX, distanceY);
        }
    });

    public GestureInterpreter(
            FullscreenControlsManager fullscreenControlsManager,
            MapMover mapMover) {
        this.fullscreenControlsManager = fullscreenControlsManager;
        this.mapMover = mapMover;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "Tap down");
        flinger.stop();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "Flinging " + velocityX + ", " + velocityY);
        flinger.fling(velocityX, velocityY);
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "Tap up");
        fullscreenControlsManager.toggleControls();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "Double tap");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "Confirmed single tap");
        return false;
    }
}
