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

package io.github.marcocipriani01.telescopetouch.activities.util;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.SensorManager;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * Created by johntaylor on 4/24/16.
 */
public class SensorAccuracyDecoder {

    private final Resources resources;
    private final Context context;

    @Inject
    public SensorAccuracyDecoder(Context context) {
        this.context = context;
        this.resources = context.getResources();
    }

    public String getTextForAccuracy(int accuracy) {
        String accuracyTxt = context.getString(R.string.sensor_accuracy_unknown);
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyTxt = context.getString(R.string.sensor_accuracy_unreliable);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyTxt = context.getString(R.string.sensor_accuracy_low);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyTxt = context.getString(R.string.sensor_accuracy_medium);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyTxt = context.getString(R.string.sensor_accuracy_high);
                break;
        }
        return accuracyTxt;
    }

    public int getColorForAccuracy(int accuracy) {
        int accuracyColor = resources.getColor(R.color.bad_sensor);
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyColor = resources.getColor(R.color.bad_sensor);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyColor = resources.getColor(R.color.low_accuracy);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyColor = resources.getColor(R.color.medium_accuracy);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyColor = resources.getColor(R.color.high_accuracy);
                break;
        }
        return accuracyColor;
    }
}