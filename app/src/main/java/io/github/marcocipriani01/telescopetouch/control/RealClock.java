package io.github.marcocipriani01.telescopetouch.control;

/**
 * Provides the current time.
 *
 * @author John Taylor
 */
public class RealClock implements Clock {

    @Override
    public long getTimeInMillisSinceEpoch() {
        // TODO(johntaylor): consider using SystemClock class.
        return System.currentTimeMillis();
    }
}