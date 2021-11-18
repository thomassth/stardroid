// Copyright 2009 Google Inc.
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
package com.google.android.stardroid.renderer

import android.opengl.GLSurfaceView
import com.google.android.stardroid.renderer.RendererController.AtomicSection
import com.google.android.stardroid.renderer.RendererControllerBase.EventQueuer
import java.util.*

/**
 * Allows the rest of the program to communicate with the SkyRenderer by queueing
 * events.
 * @author James Powell
 */
class RendererController(renderer: SkyRenderer?, view: GLSurfaceView) :
    RendererControllerBase(renderer) {
    /**
     * Used for grouping renderer calls into atomic units.
     */
    class AtomicSection(renderer: SkyRenderer) :
        RendererControllerBase(renderer) {
        private var mQueuer = Queuer()
        private var mID = 0
        override fun getQueuer(): EventQueuer {
            return mQueuer
        }

        override fun toString(): String {
            return "AtomicSection$mID"
        }

        fun releaseEvents(): Queue<Runnable> {
            val queue = mQueuer.mQueue
            mQueuer = Queuer()
            return queue
        }

        private class Queuer : EventQueuer {
            val mQueue: Queue<Runnable> = LinkedList()
            override fun queueEvent(r: Runnable) {
                mQueue.add(r)
            }
        }

        companion object {
            private var NEXT_ID = 0
        }

        init {
            synchronized(AtomicSection::class.java) { mID = NEXT_ID++ }
        }
    }

    private val mQueuer: EventQueuer
    override fun getQueuer(): EventQueuer {
        return mQueuer
    }

    override fun toString(): String {
        return "RendererController"
    }

    fun createAtomic(): AtomicSection {
        return AtomicSection(mRenderer)
    }

    fun queueAtomic(atomic: AtomicSection) {
        val msg = "Applying $atomic"
        queueRunnable(msg, CommandType.Synchronization) {
            val events = atomic.releaseEvents()
            for (r in events) {
                r.run()
            }
        }
    }

    init {
        mQueuer = EventQueuer { r: Runnable? -> view.queueEvent(r) }
    }
}