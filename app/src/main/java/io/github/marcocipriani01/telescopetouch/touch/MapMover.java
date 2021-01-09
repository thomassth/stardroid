package io.github.marcocipriani01.telescopetouch.touch;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.control.ControllerGroup;
import io.github.marcocipriani01.telescopetouch.util.Geometry;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Applies drags, zooms and rotations to the model.
 * Listens for events from the DragRotateZoomGestureDetector.
 *
 * @author John Taylor
 */
public class MapMover implements
        DragRotateZoomGestureDetector.DragRotateZoomGestureDetectorListener {

    private static final String TAG = MiscUtil.getTag(MapMover.class);
    private final AstronomerModel model;
    private final ControllerGroup controllerGroup;
    private final float sizeTimesRadiansToDegrees;

    public MapMover(AstronomerModel model, ControllerGroup controllerGroup, Context context) {
        this.model = model;
        this.controllerGroup = controllerGroup;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenLongSize = metrics.heightPixels;
        Log.i(TAG, "Screen height is " + screenLongSize + " pixels.");
        sizeTimesRadiansToDegrees = screenLongSize * Geometry.RADIANS_TO_DEGREES;
    }

    @Override
    public boolean onDrag(float xPixels, float yPixels) {
        // Log.d(TAG, "Dragging by " + xPixels + ", " + yPixels);
        final float pixelsToRadians = model.getFieldOfView() / sizeTimesRadiansToDegrees;
        controllerGroup.changeUpDown(-yPixels * pixelsToRadians);
        controllerGroup.changeRightLeft(-xPixels * pixelsToRadians);
        return true;
    }

    @Override
    public boolean onRotate(float degrees) {
        controllerGroup.rotate(-degrees);
        return true;
    }

    @Override
    public boolean onStretch(float ratio) {
        controllerGroup.zoomBy(1.0f / ratio);
        return true;
    }
}