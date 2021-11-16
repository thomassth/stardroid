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
import com.google.android.stardroid.util.MiscUtil.getTag
import com.google.android.stardroid.control.TimeTravelClock
import com.google.android.stardroid.control.TransitioningCompositeClock
import com.google.android.stardroid.util.MiscUtil
import java.util.*

/**
 * A clock that knows how to transition between a [TimeTravelClock]
 * and another [Clock].  Usually this other
 * Clock will be a [RealClock].
 *
 * @author John Taylor
 */
class TransitioningCompositeClock
/**
 * Constructor.
 *
 * The realClock parameter serves two purposes - both as the clock to query
 * when in realtime mode, and also to count the beats during the transition
 * between realtime and timetravel modes to ensure a smooth transition.
 */(
    private val timeTravelClock: TimeTravelClock,
    private val realClock: Clock
) : Clock {
    private enum class Mode {
        REAL_TIME, TRANSITION, TIME_TRAVEL
    }

    private var mode = Mode.REAL_TIME
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var startTransitionWallTime: Long = 0
    private var transitionTo: Mode? = null
    fun goTimeTravel(targetDate: Date) {
        startTime = timeInMillisSinceEpoch
        endTime = targetDate.time
        timeTravelClock.setTimeTravelDate(targetDate)
        mode = Mode.TRANSITION
        transitionTo = Mode.TIME_TRAVEL
        startTransitionWallTime = realClock.timeInMillisSinceEpoch
    }

    fun returnToRealTime() {
        startTime = timeInMillisSinceEpoch
        endTime = realClock.timeInMillisSinceEpoch + TRANSITION_TIME_MILLIS
        mode = Mode.TRANSITION
        transitionTo = Mode.REAL_TIME
        startTransitionWallTime = realClock.timeInMillisSinceEpoch
    }

    // While this will never happen - if it does let's just return real time.
    override val timeInMillisSinceEpoch: Long
        get() {
            if (mode == Mode.TRANSITION) {
                val elapsedTimeMillis = realClock.timeInMillisSinceEpoch - startTransitionWallTime
                mode = if (elapsedTimeMillis > TRANSITION_TIME_MILLIS) {
                    transitionTo!!
                } else {
                    return interpolate(
                        startTime.toDouble(), endTime.toDouble(),
                        elapsedTimeMillis.toDouble() / TRANSITION_TIME_MILLIS
                    )
                        .toLong()
                }
            }
            when (mode) {
                Mode.REAL_TIME -> return realClock.timeInMillisSinceEpoch
                Mode.TIME_TRAVEL -> return timeTravelClock.timeInMillisSinceEpoch
            }
            Log.e(TAG, "Mode is neither realtime or timetravel - this should never happen")
            // While this will never happen - if it does let's just return real time.
            return realClock.timeInMillisSinceEpoch
        }

    companion object {
        const val TRANSITION_TIME_MILLIS = 2500L
        private val TAG = getTag(TransitioningCompositeClock::class.java)

        /**
         * An interpolation function to smoothly interpolate between start
         * at lambda = 0 and end at lambda = 1
         */
        fun interpolate(start: Double, end: Double, lambda: Double): Double {
            return start + (3 * lambda * lambda - 2 * lambda * lambda * lambda) * (end - start)
        }
    }
}