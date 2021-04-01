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

package io.github.marcocipriani01.telescopetouch.activities.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Controls an activity's illumination levels.
 *
 * @author John Taylor
 * @author marcocipriani01
 */
public class DarkerModeManager implements OnSharedPreferenceChangeListener {

    private static final String TAG = TelescopeTouchApp.getTag(DarkerModeManager.class);
    private static final float BRIGHTNESS_DIM = 15f / 255f;
    private final SharedPreferences preferences;
    private final NightModeListener nightModeListener;
    private final Window window;
    private final ContentResolver contentResolver;
    private boolean nightMode = false;

    public DarkerModeManager(Activity activity, NightModeListener nightModeListener, SharedPreferences preferences) {
        this.window = activity.getWindow();
        contentResolver = activity.getContentResolver();
        this.nightModeListener = nightModeListener;
        this.preferences = preferences;
    }

    private void update() {
        nightMode = preferences.getBoolean(ApplicationConstants.DARKER_MODE_KEY, false);
        if (nightModeListener != null) nightModeListener.setNightMode(nightMode);
        WindowManager.LayoutParams params = window.getAttributes();
        if (nightMode) {
            try {
                float brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
                if (brightness > BRIGHTNESS_DIM)
                    params.screenBrightness = BRIGHTNESS_DIM;
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                params.screenBrightness = BRIGHTNESS_DIM;
            }
            params.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        } else {
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            params.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }
        window.setAttributes(params);
    }

    public boolean getPref() {
        return preferences.getBoolean(ApplicationConstants.DARKER_MODE_KEY, false);
    }

    public void start() {
        preferences.registerOnSharedPreferenceChangeListener(this);
        update();
    }

    public void stop() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean toggle() {
        boolean b = !nightMode;
        preferences.edit().putBoolean(ApplicationConstants.DARKER_MODE_KEY, b).apply();
        return b;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(ApplicationConstants.DARKER_MODE_KEY)) update();
    }

    /**
     * Activities that have some kind of custom night mode (rather than just dimming the screen) implement this.
     *
     * @author John Taylor
     */
    public interface NightModeListener {
        void setNightMode(boolean nightMode);
    }
}