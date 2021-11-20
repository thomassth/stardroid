// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.util.smoothers

import com.google.android.stardroid.util.MiscUtil.getTag
import javax.inject.Inject
import com.google.android.stardroid.control.AstronomerModel
import android.content.SharedPreferences
import android.hardware.SensorListener
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.ApplicationConstants
import android.hardware.SensorManager
import android.util.Log
import com.google.android.stardroid.util.smoothers.PlainSmootherModelAdaptor
import com.google.android.stardroid.util.MiscUtil

/**
 * Adapts sensor output for use with the astronomer model.
 *
 * @author John Taylor
 */
class PlainSmootherModelAdaptor @Inject internal constructor(
    private val model: AstronomerModel,
    sharedPreferences: SharedPreferences
) : SensorListener {
    private val magneticValues = ApplicationConstants.INITIAL_SOUTH.copyForJ()
    private val acceleration = ApplicationConstants.INITIAL_DOWN.copyForJ()
    private val reverseMagneticZaxis: Boolean
    override fun onSensorChanged(sensor: Int, values: FloatArray) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            acceleration.x = values[0]
            acceleration.y = values[1]
            acceleration.z = values[2]
        } else if (sensor == SensorManager.SENSOR_MAGNETIC_FIELD) {
            magneticValues.x = values[0]
            magneticValues.y = values[1]
            // The z direction for the mag magneticField sensor is in the opposite
            // direction to that for accelerometer, except on some phones that are doing it wrong.
            // Yes that's right, the right thing to do is to invert it.  So if we reverse that,
            // we don't invert it.  Got it?
            // TODO(johntaylor): this might not be the best place to do this.
            magneticValues.z = if (reverseMagneticZaxis) values[2] else -values[2]
        } else {
            Log.e(TAG, "Pump is receiving values that aren't accel or magnetic")
        }
        model.setPhoneSensorValues(acceleration, magneticValues)
    }

    override fun onAccuracyChanged(sensor: Int, accuracy: Int) {
        // Do nothing, at present.
    }

    companion object {
        private val TAG = getTag(PlainSmootherModelAdaptor::class.java)
    }

    init {
        reverseMagneticZaxis = sharedPreferences.getBoolean(
            ApplicationConstants.REVERSE_MAGNETIC_Z_PREFKEY, false
        )
    }
}