package io.github.marcocipriani01.telescopetouch.util;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;

/**
 * A collection of miscellaneous utility functions.
 *
 * @author Brent Bryan
 */
public class MiscUtil {
    private MiscUtil() {
    }

    /**
     * Returns the Tag for a class to be used in Android logging statements
     */
    public static String getTag(Object o) {
        if (o instanceof Class<?>) {
            return ApplicationConstants.APP_NAME + "." + ((Class<?>) o).getSimpleName();
        }
        return ApplicationConstants.APP_NAME + "." + o.getClass().getSimpleName();
    }
}