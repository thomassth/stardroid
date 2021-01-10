package io.github.marcocipriani01.telescopetouch.control;

import android.util.Log;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel.Pointing;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.Vector3;
import io.github.marcocipriani01.telescopetouch.util.VectorUtil;

/**
 * Flies the user to the search target in manual mode.
 *
 * @author John Taylor
 */
public class TeleportingController extends AbstractController {

    private static final String TAG = TelescopeTouchApplication.getTag(TeleportingController.class);

    /**
     * Teleport the astronomer instantaneously from his current pointing to a new
     * one.
     *
     * @param targetXyz The destination pointing.
     */
    public void teleport(final GeocentricCoordinates targetXyz) {
        Log.d(TAG, "Teleporting to target " + targetXyz);
        Pointing pointing = model.getPointing();
        final GeocentricCoordinates hereXyz = pointing.getLineOfSight();
        if (targetXyz.equals(hereXyz)) {
            return;
        }

        // Here we calculate the new direction of 'up' along the screen in
        // celestial coordinates.  This is not uniquely defined - it just needs
        // to be perpendicular to the target (which is effectively the normal into
        // the screen in celestial coordinates.)
        Vector3 hereTopXyz = pointing.getPerpendicular();
        hereTopXyz.normalize();
        final Vector3 normal = VectorUtil.crossProduct(hereXyz, hereTopXyz);
        Vector3 newUpXyz = VectorUtil.crossProduct(normal, targetXyz);

        model.setPointing(targetXyz, newUpXyz);
    }

    @Override
    public void start() {
        // Nothing to do.
    }

    @Override
    public void stop() {
        // Nothing to do.
        // We could consider aborting the teleport, but it's OK for now.
    }
}