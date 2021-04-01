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