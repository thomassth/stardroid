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

package io.github.marcocipriani01.telescopetouch.activities.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.preference.PreferenceManager;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class PreferencesButton extends AppCompatImageButton
        implements android.view.View.OnClickListener, OnSharedPreferenceChangeListener {

    private static final String TAG = TelescopeTouchApp.getTag(PreferencesButton.class);
    private OnClickListener secondaryOnClickListener;
    private Drawable imageOn;
    private Drawable imageOff;
    private boolean isOn;
    private String prefKey;
    private SharedPreferences preferences;
    private boolean defaultValue;

    public PreferencesButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttrs(context, attrs);
        init();
    }

    public PreferencesButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(context, attrs);
        init();
    }

    public PreferencesButton(Context context) {
        super(context);
        init();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.secondaryOnClickListener = l;
    }

    public void setAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PreferencesButton);
        imageOn = typedArray.getDrawable(R.styleable.PreferencesButton_image_on);
        imageOff = typedArray.getDrawable(R.styleable.PreferencesButton_image_off);
        prefKey = typedArray.getString(R.styleable.PreferencesButton_pref_key);
        defaultValue = typedArray.getBoolean(R.styleable.PreferencesButton_default_value, true);
        typedArray.recycle();
        Log.d(TAG, "Preference key is " + prefKey);
    }

    private void init() {
        super.setOnClickListener(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
        this.isOn = preferences.getBoolean(prefKey, defaultValue);
        Log.d(TAG, "Setting initial value of preference " + prefKey + " to " + isOn);
        setVisuallyOnOrOff();
    }

    private void setVisuallyOnOrOff() {
        setImageDrawable(isOn ? imageOn : imageOff);
    }

    private void setPreference() {
        Log.d(TAG, "Setting preference " + prefKey + " to... " + isOn);
        if (prefKey != null) {
            preferences.edit().putBoolean(prefKey, isOn).apply();
        }
    }

    @Override
    public void onClick(View v) {
        isOn = !isOn;
        setVisuallyOnOrOff();
        setPreference();
        if (secondaryOnClickListener != null) secondaryOnClickListener.onClick(v);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String changedKey) {
        if (changedKey.equals(prefKey)) {
            isOn = sharedPreferences.getBoolean(changedKey, isOn);
            setVisuallyOnOrOff();
        }
    }
}