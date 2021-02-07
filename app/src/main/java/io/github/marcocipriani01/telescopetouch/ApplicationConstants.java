/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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

package io.github.marcocipriani01.telescopetouch;

import io.github.marcocipriani01.telescopetouch.maths.Vector3;

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
    public static final String AUTO_MODE_PREF = "auto_mode";
    public static final String NO_WARN_MISSING_SENSORS_PREF = "no warn about missing sensors";
    public static final String BUNDLE_TARGET_NAME = "target_name";
    public static final String DISABLE_GYRO_PREF = "disable_gyro";
    public static final String REVERSE_MAGNETIC_Z_PREF = "reverse_magnetic_z";
    public static final String ROTATE_HORIZON_PREF = "rotate_horizon";
    public static final String LONGITUDE_PREF = "longitude";
    public static final String LATITUDE_PREF = "latitude";
    public static final String INDI_SERVERS_PREF = "INDI_SERVERS_PREF";
    public static final String INDI_PORT_PREF = "indi_port";
    public static final String NSD_PREF = "enable_nsd";
    public static final String POLARIS_HEMISPHERE_PREF = "polaris_hemisphere_selection";
    public static final String POLARIS_RETICLE_PREF = "polaris_reticle";
    public static final String MAGNETIC_DECLINATION_PREF = "use_magnetic_correction";
    public static final String NO_AUTO_LOCATE_PREF = "no_auto_locate";
    public static final String FORCE_GPS_PREF = "force_gps";
    public static final String COMPENSATE_PRECESSION_PREF = "compensate_precession";
    public static final String CATALOG_LIMIT_MAGNITUDE = "catalog_limit_mag";
    public static final String RECEIVE_BLOB_PREF = "receive_blob";
    public static final String STRETCH_FITS_PREF = "stretch_fits";
    public static final String SHOW_STARS_PREF = "show_stars_catalog";
    public static final String SHOW_DSO_PREF = "show_dso_catalog";
    public static final String SHOW_PLANETS_PREF = "show_planets_catalog";
    public static final String ONLY_VISIBLE_OBJECTS_PREF = "only_visible_catalog";
    public static final String NO_SHOW_CALIBRATION_DIALOG_PREF = "no_calibration_dialog";
}