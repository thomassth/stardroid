// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.views

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.stardroid.R
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.AnalyticsInterface
import com.google.android.stardroid.util.AnalyticsInterface.PREFERENCE_BUTTON_TOGGLE_EVENT
import com.google.android.stardroid.util.AnalyticsInterface.PREFERENCE_BUTTON_TOGGLE_VALUE
import com.google.android.stardroid.util.MiscUtil.getTag
import com.google.android.stardroid.views.PreferencesButton

class PreferencesButton : androidx.appcompat.widget.AppCompatImageButton, View.OnClickListener, OnSharedPreferenceChangeListener {
    private var secondaryOnClickListener: OnClickListener? = null
    override fun setOnClickListener(l: OnClickListener?) {
        secondaryOnClickListener = l
    }

    private var imageOn: Drawable? = null
    private var imageOff: Drawable? = null
    private var isOn = false
    private var prefKey: String? = null
    private var preferences: SharedPreferences? = null
    private var defaultValue = false

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setAttrs(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    fun setAttrs(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PreferencesButton)
        imageOn = a.getDrawable(R.styleable.PreferencesButton_image_on)
        imageOff = a.getDrawable(R.styleable.PreferencesButton_image_off)
        prefKey = a.getString(R.styleable.PreferencesButton_pref_key)
        defaultValue = a.getBoolean(R.styleable.PreferencesButton_default_value, true)
        Log.d(TAG, "Preference key is $prefKey")
    }

    private fun init() {
        super.setOnClickListener(this)
        preferences = PreferenceManager.getDefaultSharedPreferences(context) as SharedPreferences
        preferences!!.registerOnSharedPreferenceChangeListener(this)
        isOn = preferences!!.getBoolean(prefKey, defaultValue)
        Log.d(TAG, "Setting initial value of preference $prefKey to $isOn")
        setVisuallyOnOrOff()
    }

    private fun setVisuallyOnOrOff() {
        setImageDrawable(if (isOn) imageOn else imageOff)
    }

    private fun setPreference() {
        Log.d(TAG, "Setting preference $prefKey to... $isOn")
        if (prefKey != null) {
            preferences!!.edit().putBoolean(prefKey, isOn).apply()
        }
    }

    override fun onClick(v: View) {
        isOn = !isOn
        if (analytics != null) {
            val b = Bundle()
            b.putString(PREFERENCE_BUTTON_TOGGLE_VALUE, "$prefKey:$isOn")
            analytics!!.trackEvent(PREFERENCE_BUTTON_TOGGLE_EVENT, b)
        }
        setVisuallyOnOrOff()
        setPreference()
        if (secondaryOnClickListener != null) {
            secondaryOnClickListener!!.onClick(v)
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        changedKey: String
    ) {
        if (changedKey != null && changedKey == prefKey) {
            isOn = sharedPreferences.getBoolean(changedKey, isOn)
            setVisuallyOnOrOff()
        }
    }

    companion object {
        private val TAG = getTag(PreferencesButton::class.java)
        private var analytics: AnalyticsInterface? = null

        /**
         * Sets the [Analytics] instance for reporting preference toggles.
         *
         * This class gets instantiated by the system and there's not obvious way to access anything
         * dagger-ey to inject the [Analytics].  Since it's not vital to the class'
         * functioning and we'll probably kill this class anyway at some point I can live with this
         * hack.
         * @param analytics
         */
        @JvmStatic
        fun setAnalytics(analytics: AnalyticsInterface?) {
            Companion.analytics = analytics
        }
    }
}