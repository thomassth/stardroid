package io.github.marcocipriani01.telescopetouch.views;

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
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

public class PreferencesButton extends AppCompatImageButton
        implements android.view.View.OnClickListener, OnSharedPreferenceChangeListener {
    private static final String TAG = MiscUtil.getTag(PreferencesButton.class);
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferencesButton);
        imageOn = a.getDrawable(R.styleable.PreferencesButton_image_on);
        imageOff = a.getDrawable(R.styleable.PreferencesButton_image_off);
        prefKey = a.getString(R.styleable.PreferencesButton_pref_key);
        defaultValue = a.getBoolean(R.styleable.PreferencesButton_default_value, true);
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
        if (secondaryOnClickListener != null) {
            secondaryOnClickListener.onClick(v);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String changedKey) {
        if (changedKey.equals(prefKey)) {
            isOn = sharedPreferences.getBoolean(changedKey, isOn);
            setVisuallyOnOrOff();
        }
    }
}
