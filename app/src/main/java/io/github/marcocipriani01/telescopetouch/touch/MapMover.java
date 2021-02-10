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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.widget.Toast;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.control.ControllerGroup;
import io.github.marcocipriani01.telescopetouch.maths.MathsUtils;

/**
 * Applies drags, zooms and rotations to the model.
 * Listens for events from the DragRotateZoomGestureDetector.
 *
 * @author John Taylor
 */
public class MapMover implements DragRotateZoomGestureDetector.DragRotateZoomGestureDetectorListener {

    private static final int MANUAL_MODE_THRESHOLD = 100;
    private final AstronomerModel model;
    private final ControllerGroup controllerGroup;
    private final float sizeTimesRadiansToDegrees;
    private final Context context;
    private final SharedPreferences preferences;

    public MapMover(AstronomerModel model, ControllerGroup controllerGroup, Context context, SharedPreferences preferences) {
        this.model = model;
        this.controllerGroup = controllerGroup;
        this.context = context;
        this.preferences = preferences;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenLongSize = metrics.heightPixels;
        //Log.i(TAG, "Screen height is " + screenLongSize + " pixels.");
        sizeTimesRadiansToDegrees = (float) (screenLongSize * MathsUtils.RADIANS_TO_DEGREES);
    }

    @Override
    public void onDrag(float xPixels, float yPixels) {
        if (controllerGroup.isAutoMode() &&
                ((Math.abs(xPixels) > MANUAL_MODE_THRESHOLD || Math.abs(yPixels) > MANUAL_MODE_THRESHOLD))) {
            Toast.makeText(context, "Toggling manual mode", Toast.LENGTH_SHORT).show();
            preferences.edit().putBoolean(ApplicationConstants.AUTO_MODE_PREF, false).apply();
        }
        // Log.d(TAG, "Dragging by " + xPixels + ", " + yPixels);
        final float pixelsToRadians = model.getFieldOfView() / sizeTimesRadiansToDegrees;
        controllerGroup.changeUpDown(-yPixels * pixelsToRadians);
        controllerGroup.changeRightLeft(-xPixels * pixelsToRadians);
    }

    @Override
    public void onRotate(float degrees) {
        controllerGroup.rotate(-degrees);
    }

    @Override
    public void onStretch(float ratio) {
        controllerGroup.zoomBy(1.0f / ratio);
    }
}