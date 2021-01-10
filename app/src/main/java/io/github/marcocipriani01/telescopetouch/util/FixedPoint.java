package io.github.marcocipriani01.telescopetouch.util;

public final class FixedPoint {

    public static final int ONE = 0x00010000;

    private FixedPoint() {
    }

    /// Converts a float to a 16.16 fixed point number 
    public static int floatToFixedPoint(float f) {
        return (int) (65536F * f);
    }
}