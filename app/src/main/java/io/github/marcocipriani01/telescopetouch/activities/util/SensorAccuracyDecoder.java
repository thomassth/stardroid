/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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