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

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.android.material.snackbar.Snackbar;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
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
    private final Activity activity;
    private final SharedPreferences preferences;

    public MapMover(AstronomerModel model, ControllerGroup controllerGroup, Activity activity, SharedPreferences preferences) {
        this.model = model;
        this.controllerGroup = controllerGroup;
        this.activity = activity;
        int screenLongSize = activity.getResources().getDisplayMetrics().heightPixels;
        this.preferences = preferences;
        sizeTimesRadiansToDegrees = (float) (screenLongSize * MathsUtils.RADIANS_TO_DEGREES);
    }

    @Override
    public void onDrag(float xPixels, float yPixels) {
        if (controllerGroup.isAutoMode() &&
                ((Math.abs(xPixels) > MANUAL_MODE_THRESHOLD || Math.abs(yPixels) > MANUAL_MODE_THRESHOLD))) {
            Snackbar.make(activity.getWindow().getDecorView().getRootView(),
                    activity.getString(R.string.toggling_manual_mode), Snackbar.LENGTH_SHORT).show();
            preferences.edit().putBoolean(ApplicationConstants.AUTO_MODE_PREF, false).apply();
        }
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