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
    private static final float BRIGHTNESS_DIM = 20f / 255f;
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