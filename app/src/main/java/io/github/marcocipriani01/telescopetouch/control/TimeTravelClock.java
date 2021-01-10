package io.github.marcocipriani01.telescopetouch.control;

import android.util.Log;

import java.util.Date;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;
import io.github.marcocipriani01.telescopetouch.base.TimeConstants;

/**
 * Controls time as selected / created by the user in Time Travel mode.
 * Includes control for "playing" through time in both directions at different
 * speeds.
 *
 * @author Dominic Widdows
 * @author John Taylor
 */
public class TimeTravelClock implements Clock {
    public static final long STOPPED = 0;
    private static final Speed[] SPEEDS = {
            new Speed(-TimeConstants.SECONDS_PER_WEEK, R.string.time_travel_week_speed_back),
            new Speed(-TimeConstants.SECONDS_PER_DAY, R.string.time_travel_day_speed_back),
            new Speed(-TimeConstants.SECONDS_PER_HOUR, R.string.time_travel_hour_speed_back),
            new Speed(-TimeConstants.SECONDS_PER_10MINUTE, R.string.time_travel_10minute_speed_back),
            new Speed(-TimeConstants.SECONDS_PER_MINUTE, R.string.time_travel_minute_speed_back),
            new Speed(-TimeConstants.SECONDS_PER_SECOND, R.string.time_travel_second_speed_back),
            new Speed(STOPPED, R.string.time_travel_stopped),
            new Speed(TimeConstants.SECONDS_PER_SECOND, R.string.time_travel_second_speed),
            new Speed(TimeConstants.SECONDS_PER_MINUTE, R.string.time_travel_minute_speed),
            new Speed(TimeConstants.SECONDS_PER_10MINUTE, R.string.time_travel_10minute_speed),
            new Speed(TimeConstants.SECONDS_PER_HOUR, R.string.time_travel_hour_speed),
            new Speed(TimeConstants.SECONDS_PER_DAY, R.string.time_travel_day_speed),
            new Speed(TimeConstants.SECONDS_PER_WEEK, R.string.time_travel_week_speed),
    };
    private static final int STOPPED_INDEX = SPEEDS.length / 2;
    private static final String TAG = TelescopeTouchApplication.getTag(TimeTravelClock.class);
    private int speedIndex = STOPPED_INDEX;
    private long timeLastSet;
    private long simulatedTime;

    /**
     * Sets the internal time.
     *
     * @param date Date to which the timeTravelDate will be set.
     */
    public synchronized void setTimeTravelDate(Date date) {
        pauseTime();
        timeLastSet = System.currentTimeMillis();
        simulatedTime = date.getTime();
    }

    /**
     * Increases the rate of time travel into the future
     * (or decreases the rate of time travel into the past.)
     */
    public synchronized void accelerateTimeTravel() {
        if (speedIndex < SPEEDS.length - 1) {
            Log.d(TAG, "Accelerating speed to: " + SPEEDS[speedIndex]);
            ++speedIndex;
        } else {
            Log.d(TAG, "Already at max forward speed");
        }
    }

    /*
     * Controller logic for playing through time at different directions and
     * speeds.
     */

    /**
     * Decreases the rate of time travel into the future
     * (or increases the rate of time travel into the past.)
     */
    public synchronized void decelerateTimeTravel() {
        if (speedIndex > 0) {
            Log.d(TAG, "Decelerating speed to: " + SPEEDS[speedIndex]);
            --speedIndex;
        } else {
            Log.d(TAG, "Already at maximum backwards speed");
        }
    }

    /**
     * Pauses time.
     */
    public synchronized void pauseTime() {
        Log.d(TAG, "Pausing time");
        assert SPEEDS[STOPPED_INDEX].rate == 0.0;
        speedIndex = STOPPED_INDEX;
    }

    /**
     * @return The current speed tag, a string describing the speed of time
     * travel.
     */
    public int getCurrentSpeedTag() {
        return SPEEDS[speedIndex].labelTag;
    }

    @Override
    public long getTimeInMillisSinceEpoch() {
        long now = System.currentTimeMillis();
        long elapsedTimeMillis = now - timeLastSet;
        double rate = SPEEDS[speedIndex].rate;
        long timeDelta = (long) (rate * elapsedTimeMillis);
        if (Math.abs(rate) >= TimeConstants.SECONDS_PER_DAY) {
            // For speeds greater than or equal to 1 day/sec we want to move in
            // increments of 1 day so that the map isn't dizzyingly fast.
            // This shows the slow annual procession of the stars.
            long days = timeDelta / TimeConstants.MILLISECONDS_PER_DAY;
            if (days == 0) {
                return simulatedTime;
            }
            // Note that this assumes that time requests will occur right on the
            // day boundary.  If they occur later then the next time jump
            // might be a bit shorter than it should be.  Nevertheless the refresh
            // rate of the renderer is high enough that this should be unnoticeable.
            timeDelta = days * TimeConstants.MILLISECONDS_PER_DAY;
        }
        timeLastSet = now;
        simulatedTime += timeDelta;
        return simulatedTime;
    }

    /**
     * A data holder for the time stepping speeds.
     */
    private static class Speed {
        /**
         * The speed in seconds per second.
         */
        public double rate;
        /**
         * The id of the Speed's string label.
         */
        public int labelTag;

        public Speed(double rate, int labelTag) {
            this.rate = rate;
            this.labelTag = labelTag;
        }
    }
}
