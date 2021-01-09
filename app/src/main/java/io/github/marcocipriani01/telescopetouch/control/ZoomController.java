package io.github.marcocipriani01.telescopetouch.control;

import android.util.Log;

import io.github.marcocipriani01.telescopetouch.base.VisibleForTesting;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Controls the field of view of a user.
 *
 * @author John Taylor
 */
public class ZoomController extends AbstractController {

    @VisibleForTesting
    public static final float MAX_ZOOM_OUT = 90.0f;
    private static final String TAG = MiscUtil.getTag(ZoomController.class);

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
        float zoomDegrees = model.getFieldOfView();
        zoomDegrees = Math.min(zoomDegrees * ratio, MAX_ZOOM_OUT);
        setFieldOfView(zoomDegrees);
    }
}