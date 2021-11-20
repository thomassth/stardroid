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
package com.google.android.stardroid.control

import android.util.Log
import com.google.android.stardroid.R
import com.google.android.stardroid.base.TimeConstants.MILLISECONDS_PER_DAY
import com.google.android.stardroid.base.TimeConstants.SECONDS_PER_10MINUTE
import com.google.android.stardroid.base.TimeConstants.SECONDS_PER_DAY
import com.google.android.stardroid.base.TimeConstants.SECONDS_PER_MINUTE
import com.google.android.stardroid.base.TimeConstants.SECONDS_PER_SECOND
import com.google.android.stardroid.base.TimeConstants.SECONDS_PER_WEEK
import com.google.android.stardroid.control.TimeTravelClock
import com.google.android.stardroid.math.SECONDS_PER_HOUR
import com.google.android.stardroid.util.MiscUtil.getTag
import java.util.*

/**
 * Controls time as selected / created by the user in Time Travel mode.
 * Includes control for "playing" through time in both directions at different
 * speeds.
 *
 * @author Dominic Widdows
 * @author John Taylor
 */
class TimeTravelClock : Clock {
    /**
     * A data holder for the time stepping speeds.
     */
    private class Speed
    /**
     * @return The current speed tag, a string describing the speed of time
     * travel.
     */(
        /** The speed in seconds per second.  */
        var rate: Double,
        /** The id of the Speed's string label.  */
        var currentSpeedTag: Int
    )

    private var speedIndex = STOPPED_INDEX
    private var timeLastSet: Long = 0
    private var simulatedTime: Long = 0

    /**
     * Sets the internal time.
     * @param date Date to which the timeTravelDate will be set.
     */
    @Synchronized
    fun setTimeTravelDate(date: Date) {
        pauseTime()
        timeLastSet = System.currentTimeMillis()
        simulatedTime = date.time
    }
    /*
   * Controller logic for playing through time at different directions and
   * speeds.
   */
    /**
     * Increases the rate of time travel into the future
     * (or decreases the rate of time travel into the past.)
     */
    @Synchronized
    fun accelerateTimeTravel() {
        if (speedIndex < SPEEDS.size - 1) {
            Log.d(TAG, "Accelerating speed to: " + SPEEDS[speedIndex])
            ++speedIndex
        } else {
            Log.d(TAG, "Already at max forward speed")
        }
    }


    fun getCurrentSpeedTag(): Int {
        return SPEEDS[speedIndex].currentSpeedTag;
    }

    /**
     * Decreases the rate of time travel into the future
     * (or increases the rate of time travel into the past.)
     */
    @Synchronized
    fun decelerateTimeTravel() {
        if (speedIndex > 0) {
            Log.d(TAG, "Decelerating speed to: " + SPEEDS[speedIndex])
            --speedIndex
        } else {
            Log.d(TAG, "Already at maximum backwards speed")
        }
    }

    /**
     * Pauses time.
     */
    @Synchronized
    fun pauseTime() {
        Log.d(TAG, "Pausing time")
        assert(SPEEDS[STOPPED_INDEX].rate == 0.0)
        speedIndex = STOPPED_INDEX
    }

    // For speeds greater than or equal to 1 day/sec we want to move in
    // increments of 1 day so that the map isn't dizzyingly fast.
    // This shows the slow annual procession of the stars.
    override val timeInMillisSinceEpoch: Long
        // Note that this assumes that time requests will occur right on the
        // day boundary.  If they occur later then the next time jump
        // might be a bit shorter than it should be.  Nevertheless the refresh
        // rate of the renderer is high enough that this should be unnoticeable.
        get() {
            val now = System.currentTimeMillis()
            val elapsedTimeMillis = now - timeLastSet
            val rate = SPEEDS[speedIndex].rate
            var timeDelta = (rate * elapsedTimeMillis).toLong()
            if (Math.abs(rate) >= SECONDS_PER_DAY) {
                // For speeds greater than or equal to 1 day/sec we want to move in
                // increments of 1 day so that the map isn't dizzyingly fast.
                // This shows the slow annual procession of the stars.
                val days = (timeDelta / MILLISECONDS_PER_DAY) as Long
                if (days == 0L) {
                    return simulatedTime
                }
                // Note that this assumes that time requests will occur right on the
                // day boundary.  If they occur later then the next time jump
                // might be a bit shorter than it should be.  Nevertheless the refresh
                // rate of the renderer is high enough that this should be unnoticeable.
                timeDelta = (days * MILLISECONDS_PER_DAY) as Long
            }
            timeLastSet = now
            simulatedTime += timeDelta
            return simulatedTime
        }

    companion object {
        const val STOPPED: Long = 0
        private val SPEEDS = arrayOf(
            Speed((-SECONDS_PER_WEEK).toDouble(), R.string.time_travel_week_speed_back),
            Speed((-SECONDS_PER_DAY).toDouble(), R.string.time_travel_day_speed_back),
            Speed(-SECONDS_PER_HOUR, R.string.time_travel_hour_speed_back),
            Speed((-SECONDS_PER_10MINUTE).toDouble(), R.string.time_travel_10minute_speed_back),
            Speed((-SECONDS_PER_MINUTE).toDouble(), R.string.time_travel_minute_speed_back),
            Speed((-SECONDS_PER_SECOND).toDouble(), R.string.time_travel_second_speed_back),
            Speed(STOPPED.toDouble(), R.string.time_travel_stopped),
            Speed(SECONDS_PER_SECOND.toDouble(), R.string.time_travel_second_speed),
            Speed(SECONDS_PER_MINUTE.toDouble(), R.string.time_travel_minute_speed),
            Speed(SECONDS_PER_10MINUTE.toDouble(), R.string.time_travel_10minute_speed),
            Speed(SECONDS_PER_HOUR, R.string.time_travel_hour_speed),
            Speed(SECONDS_PER_DAY.toDouble(), R.string.time_travel_day_speed),
            Speed(SECONDS_PER_WEEK.toDouble(), R.string.time_travel_week_speed)
        )
        private val STOPPED_INDEX = SPEEDS.size / 2
        private val TAG = getTag(TimeTravelClock::class.java)
    }
}