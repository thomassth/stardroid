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

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.google.android.stardroid.control.AstronomerModel
import com.google.android.stardroid.control.ZoomController
import org.junit.Before
import kotlin.Throws
import org.easymock.EasyMock
import com.google.android.stardroid.control.ZoomControllerTest
import junit.framework.TestCase
import com.google.android.stardroid.control.TransitioningCompositeClock
import com.google.android.stardroid.control.TimeTravelClock
import com.google.android.stardroid.control.TransitioningCompositeClockTest.FakeClock
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.*

/**
 * Tests for the [TransitioningCompositeClock].
 *
 * @author John Taylor
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TransitioningCompositeClockTest : TestCase() {
    /**
     * A fake clock for which we can set the time.
     *
     * @author John Taylor
     */
    private class FakeClock : Clock {
        private var time: Long = 0
        override fun getTimeInMillisSinceEpoch(): Long {
            return time
        }

        fun setTimeInMillisSinceEpoch(time: Long) {
            this.time = time
        }

        fun advanceTimeByMillis(deltaTime: Long) {
            time += deltaTime
        }
    }

    @Test
    fun testInterpolant() {
        val tol = 1e-3
        assertEquals(0.0, TransitioningCompositeClock.interpolate(0.0, 10.0, 0.0), tol)
        assertEquals(1.0, TransitioningCompositeClock.interpolate(1.0, 10.0, 0.0), tol)
        assertEquals(10.0, TransitioningCompositeClock.interpolate(0.0, 10.0, 1.0), tol)
        assertEquals(5.0, TransitioningCompositeClock.interpolate(0.0, 10.0, 0.5), tol)
        // Test derivatives
        val epsilon = 1e-4
        val dydx0 = (TransitioningCompositeClock.interpolate(0.0, 1.0, epsilon)
                - TransitioningCompositeClock.interpolate(0.0, 1.0, 0.0)) / epsilon
        assertEquals(0.0, dydx0, tol)
        val dydx1 = (TransitioningCompositeClock.interpolate(0.0, 1.0, 1.0)
                - TransitioningCompositeClock.interpolate(0.0, 1.0, 1 - epsilon)) / epsilon
        assertEquals(0.0, dydx1, tol)
    }

    @Test
    fun testTransition() {
        val timeTravelClock = TimeTravelClock()
        val fakeClock = FakeClock()
        val transitioningClock = TransitioningCompositeClock(
            timeTravelClock, fakeClock
        )
        fakeClock.timeInMillisSinceEpoch = 1000
        // Transitioning clock starts in real time
        assertEquals(1000, transitioningClock.timeInMillisSinceEpoch)
        fakeClock.timeInMillisSinceEpoch = 2000
        assertEquals(2000, transitioningClock.timeInMillisSinceEpoch)
        val timeTravelDate = Date(5000)
        transitioningClock.goTimeTravel(timeTravelDate)
        // We shouldn't have budged
        assertEquals(2000, transitioningClock.timeInMillisSinceEpoch)
        fakeClock.advanceTimeByMillis(TransitioningCompositeClock.TRANSITION_TIME_MILLIS / 2)
        // Half way there
        assertEquals(3500, transitioningClock.timeInMillisSinceEpoch)
        fakeClock.advanceTimeByMillis(TransitioningCompositeClock.TRANSITION_TIME_MILLIS / 2)
        // All the way there
        assertEquals(5000, transitioningClock.timeInMillisSinceEpoch)
        // Where we stay...
        fakeClock.advanceTimeByMillis(1000)
        assertEquals(5000, transitioningClock.timeInMillisSinceEpoch)
        transitioningClock.returnToRealTime()
        val destinationTime = (fakeClock.timeInMillisSinceEpoch
                + TransitioningCompositeClock.TRANSITION_TIME_MILLIS)
        // Shouldn't have moved yet
        assertEquals(5000, transitioningClock.timeInMillisSinceEpoch)
        fakeClock.advanceTimeByMillis(TransitioningCompositeClock.TRANSITION_TIME_MILLIS / 2)
        // Half way there
        assertEquals(
            (5000 + destinationTime) / 2,
            transitioningClock.timeInMillisSinceEpoch
        )
        fakeClock.advanceTimeByMillis(TransitioningCompositeClock.TRANSITION_TIME_MILLIS / 2)
        // All the way there
        assertEquals(
            destinationTime,
            transitioningClock.timeInMillisSinceEpoch
        )
        fakeClock.advanceTimeByMillis(1000)
        // Continue to advance in real time.
        assertEquals(
            fakeClock.timeInMillisSinceEpoch,
            transitioningClock.timeInMillisSinceEpoch
        )
    }
}