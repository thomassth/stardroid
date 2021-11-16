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
package com.google.android.stardroid.touch

import com.google.android.stardroid.math.MathUtils.sqrt
import com.google.android.stardroid.math.MathUtils.atan2
import com.google.android.stardroid.touch.Flinger.FlingListener
import com.google.android.stardroid.touch.Flinger
import com.google.android.stardroid.util.MiscUtil
import com.google.android.stardroid.control.AstronomerModel
import com.google.android.stardroid.control.ControllerGroup
import com.google.android.stardroid.touch.DragRotateZoomGestureDetector.DragRotateZoomGestureDetectorListener
import com.google.android.stardroid.touch.MapMover
import android.util.DisplayMetrics
import android.util.Log
import com.google.android.stardroid.activities.util.FullscreenControlsManager
import android.view.GestureDetector.SimpleOnGestureListener
import com.google.android.stardroid.touch.GestureInterpreter
import com.google.android.stardroid.touch.DragRotateZoomGestureDetector
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Given a flung motion event, this class pumps new Motion events out
 * to simulate an underlying object with some inertia.
 */
class Flinger(private val listener: FlingListener) {
    interface FlingListener {
        fun fling(distanceX: Float, distanceY: Float)
    }

    private val updatesPerSecond = 20
    private val timeIntervalMillis = 1000 / updatesPerSecond
    private val executor: ScheduledExecutorService
    private var flingTask: ScheduledFuture<*>? = null
    fun fling(velocityX: Float, velocityY: Float) {
        Log.d(TAG, "Doing the fling")
        class PositionUpdater(private var myVelocityX: Float, private var myVelocityY: Float) :
            Runnable {
            private val decelFactor = 1.1f
            private val TOL = 10f
            override fun run() {
                if (myVelocityX * myVelocityX + myVelocityY * myVelocityY < TOL) {
                    stop()
                }
                listener.fling(
                    myVelocityX / updatesPerSecond,
                    myVelocityY / updatesPerSecond
                )
                myVelocityX /= decelFactor
                myVelocityY /= decelFactor
            }
        }
        flingTask = executor.scheduleAtFixedRate(
            PositionUpdater(velocityX, velocityY),
            0, timeIntervalMillis.toLong(), TimeUnit.MILLISECONDS
        )
    }

    /**
     * Brings the flinger to a dead stop.
     */
    fun stop() {
        if (flingTask != null) flingTask!!.cancel(true)
        Log.d(TAG, "Fling stopped")
    }

    companion object {
        private val TAG = MiscUtil.getTag(Flinger::class.java)
    }

    init {
        executor = Executors.newScheduledThreadPool(1)
    }
}