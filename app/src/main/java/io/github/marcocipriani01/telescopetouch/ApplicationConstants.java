package io.github.marcocipriani01.telescopetouch;

import io.github.marcocipriani01.telescopetouch.units.Vector3;

/**
 * A home for the application's few global constants.
 */
public class ApplicationConstants {

    public static final String APP_NAME = "Telescope.Touch";
    /**
     * Default value for 'south' in phone coords when the app starts
     */
    public static final Vector3 INITIAL_SOUTH = new Vector3(0, -1, 0);
    /**
     * Default value for 'down' in phone coords when the app starts
     */
    public static final Vector3 INITIAL_DOWN = new Vector3(0, -1, -9);

    // Preference keys
    public static final String AUTO_MODE_PREF_KEY = "auto_mode";
    public static final String NO_WARN_ABOUT_MISSING_SENSORS = "no warn about missing sensors";
    public static final String BUNDLE_TARGET_NAME = "target_name";
    public static final String BUNDLE_NIGHT_MODE = "night_mode";
    public static final String BUNDLE_X_TARGET = "bundle_x_target";
    public static final String BUNDLE_Y_TARGET = "bundle_y_target";
    public static final String BUNDLE_Z_TARGET = "bundle_z_target";
    public static final String BUNDLE_SEARCH_MODE = "bundle_search";
    public static final String SOUND_EFFECTS = "sound_effects";
    // Preference that keeps track of whether or not the user accepted the ToS for this version
    public static final String READ_TOS_PREF_VERSION = "read_tos_version";
    public static final String SHARED_PREFERENCE_DISABLE_GYRO = "disable_gyro";
    public static final String REVERSE_MAGNETIC_Z_PREFKEY = "reverse_magnetic_z";
    public static final String ROTATE_HORIZON_PREFKEY = "rotate_horizon";
}