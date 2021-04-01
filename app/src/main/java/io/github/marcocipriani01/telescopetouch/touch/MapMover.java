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