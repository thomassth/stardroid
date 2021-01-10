package io.github.marcocipriani01.telescopetouch.control;

import android.util.Log;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Controls the field of view of a user.
 *
 * @author John Taylor
 */
public class ZoomController extends AbstractController {

    public static final float MAX_ZOOM = 90.0f;
    public static final float MIN_ZOOM = 1.5f;
    private static final String TAG = TelescopeTouchApp.getTag(ZoomController.class);

    private void setFieldOfView(float zoomDegrees) {
        if (!enabled) {
            return;
        }
        Log.d(TAG, "Setting field of view to " + zoomDegrees);
        model.setFieldOfView(zoomDegrees);
    }

    @Override
    public void start() {
        // Nothing to do
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    /**
     * Increases the field of view by the given ratio.  That is, a number >1 will zoom the user
     * out, up to a predetermined maximum.
     */
    public void zoomBy(float ratio) {
        float zoomDegrees = model.getFieldOfView() * ratio;
        if (zoomDegrees > MAX_ZOOM) {
            zoomDegrees = MAX_ZOOM;
        } else {
            zoomDegrees = Math.max(zoomDegrees, MIN_ZOOM);
        }
        setFieldOfView(zoomDegrees);
    }
}