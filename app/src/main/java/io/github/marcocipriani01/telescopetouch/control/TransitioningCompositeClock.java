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

package io.github.marcocipriani01.telescopetouch.control;

import android.util.Log;

import java.util.Date;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * A clock that knows how to transition between a {@link TimeTravelClock}
 * and another {@link Clock}.  Usually this other
 * Clock will be a {@link RealClock}.
 *
 * @author John Taylor
 */
public class TransitioningCompositeClock implements Clock {

    public static final long TRANSITION_TIME_MILLIS = 2500L;
    private static final String TAG = TelescopeTouchApp.getTag(TransitioningCompositeClock.class);
    private final Clock realClock;
    private final TimeTravelClock timeTravelClock;
    private Mode mode = Mode.REAL_TIME;
    private long startTime;
    private long endTime;
    private long startTransitionWallTime;
    private Mode transitionTo;

    /**
     * Constructor.
     * <p>
     * The realClock parameter serves two purposes - both as the clock to query
     * when in realtime mode, and also to count the beats during the transition
     * between realtime and timetravel modes to ensure a smooth transition.
     */
    public TransitioningCompositeClock(TimeTravelClock timeTravelClock, Clock realClock) {
        this.timeTravelClock = timeTravelClock;
        this.realClock = realClock;
    }

    /**
     * An interpolation function to smoothly interpolate between start
     * at lambda = 0 and end at lambda = 1
     */
    public static double interpolate(double start, double end, double lambda) {
        return (start + (3 * lambda * lambda - 2 * lambda * lambda * lambda) * (end - start));
    }

    public void goTimeTravel(Date targetDate) {
        startTime = getTimeInMillisSinceEpoch();
        endTime = targetDate.getTime();
        timeTravelClock.setTimeTravelDate(targetDate);
        mode = Mode.TRANSITION;
        transitionTo = Mode.TIME_TRAVEL;
        startTransitionWallTime = realClock.getTimeInMillisSinceEpoch();
    }

    public void returnToRealTime() {
        startTime = getTimeInMillisSinceEpoch();
        endTime = realClock.getTimeInMillisSinceEpoch() + TRANSITION_TIME_MILLIS;
        mode = Mode.TRANSITION;
        transitionTo = Mode.REAL_TIME;
        startTransitionWallTime = realClock.getTimeInMillisSinceEpoch();
    }

    @Override
    public long getTimeInMillisSinceEpoch() {
        if (mode == Mode.TRANSITION) {
            long elapsedTimeMillis = realClock.getTimeInMillisSinceEpoch() - startTransitionWallTime;
            if (elapsedTimeMillis > TRANSITION_TIME_MILLIS) {
                mode = transitionTo;
            } else {
                return (long) interpolate(startTime, endTime,
                        ((double) elapsedTimeMillis) / TRANSITION_TIME_MILLIS);
            }
        }
        switch (mode) {
            case REAL_TIME:
                return realClock.getTimeInMillisSinceEpoch();
            case TIME_TRAVEL:
                return timeTravelClock.getTimeInMillisSinceEpoch();
        }
        Log.e(TAG, "Mode is neither realtime or timetravel - this should never happen");
        // While this will never happen - if it does let's just return real time.
        return realClock.getTimeInMillisSinceEpoch();
    }

    private enum Mode {REAL_TIME, TRANSITION, TIME_TRAVEL}
}