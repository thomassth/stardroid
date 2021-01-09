package io.github.marcocipriani01.telescopetouch.activities.util;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

/**
 * Controls the brightness level of an activity.
 *
 * @author John Taylor
 */
public class ActivityLightLevelChanger {
    // This value is based on inspecting the Android source code for the
    // SettingsAppWidgetProvider:
    // http://hi-android.info/src/com/android/settings/widget/SettingsAppWidgetProvider.java.html
    // (We know that 0.05 is OK on the G1 and N1, but not some other phones, so we don't make this
    // as dim as we could...)
    private static final float BRIGHTNESS_DIM = (float) 20f / 255f;
    private final NightModeable nightModeable;
    private final Activity activity;

    /**
     * Wraps an activity with a setNightMode method.
     *
     * @param activity      the activity under control
     * @param nightmodeable Allows an activity to have a custom night mode method.  May be null.
     */
    public ActivityLightLevelChanger(Activity activity, @Nullable NightModeable nightmodeable) {
        this.activity = activity;
        this.nightModeable = nightmodeable;
    }

    // current setting.
    public void setNightMode(boolean nightMode) {
        if (nightModeable != null) {
            nightModeable.setNightMode(nightMode);
        }
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (nightMode) {
            params.screenBrightness = BRIGHTNESS_DIM;
            params.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;    // TODO(jontayler): look at this again - at present night mode can be brighter than the phone's
        } else {
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            params.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }
        window.setAttributes(params);
    }

    /**
     * Activities that have some kind of custom night mode (rather than just
     * dimming the screen) implement this.
     *
     * @author John Taylor
     */
    public interface NightModeable {
        void setNightMode(boolean nightMode);
    }
}