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

package io.github.marcocipriani01.telescopetouch.renderer;

import android.opengl.GLSurfaceView;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Allows the rest of the program to communicate with the SkyRenderer by queueing
 * events.
 *
 * @author James Powell
 */
public class RendererController extends RendererControllerBase {

    private final EventQueuer mQueuer;

    public RendererController(SkyRenderer renderer, final GLSurfaceView view) {
        super(renderer);
        mQueuer = view::queueEvent;
    }

    @Override
    protected EventQueuer getQueuer() {
        return mQueuer;
    }

    @NonNull
    @Override
    public String toString() {
        return "RendererController";
    }

    public AtomicSection createAtomic() {
        return new AtomicSection(mRenderer);
    }

    public void queueAtomic(final AtomicSection atomic) {
        String msg = "Applying " + atomic.toString();
        queueRunnable(msg, CommandType.Synchronization, () -> {
            Queue<Runnable> events = atomic.releaseEvents();
            for (Runnable r : events) {
                r.run();
            }
        });
    }

    /**
     * Used for grouping renderer calls into atomic units.
     */
    public static class AtomicSection extends RendererControllerBase {
        private static int NEXT_ID = 0;
        private final int mID;
        private Queuer mQueuer = new Queuer();

        private AtomicSection(SkyRenderer renderer) {
            super(renderer);
            synchronized (AtomicSection.class) {
                mID = NEXT_ID++;
            }
        }

        @Override
        protected EventQueuer getQueuer() {
            return mQueuer;
        }

        @NonNull
        @Override
        public String toString() {
            return "AtomicSection" + mID;
        }

        private Queue<Runnable> releaseEvents() {
            Queue<Runnable> queue = mQueuer.mQueue;
            mQueuer = new Queuer();
            return queue;
        }

        private static class Queuer implements EventQueuer {
            private final Queue<Runnable> mQueue = new LinkedList<>();

            public void queueEvent(Runnable r) {
                mQueue.add(r);
            }
        }
    }
}