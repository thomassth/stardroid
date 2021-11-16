// Copyright 2008 Google Inc.
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
import com.google.android.stardroid.source.PointPrimitive
import com.google.android.stardroid.source.TextPrimitive
import com.google.android.stardroid.source.ImagePrimitive
import com.google.android.stardroid.source.LinePrimitive
import kotlin.jvm.JvmOverloads
import android.widget.LinearLayout
import android.view.animation.AlphaAnimation
import android.content.res.TypedArray
import com.google.android.stardroid.R
import android.widget.ImageButton
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.SharedPreferences
import com.google.android.stardroid.views.PreferencesButton
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.MiscUtil
import com.google.android.stardroid.util.AnalyticsInterface

/**
 * Contains the provider buttons.
 */
class ButtonLayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    // TODO(jontayler): clear up the fade code which is no longer used.
    private var fadeTime = 500
    override fun onTouchEvent(event: MotionEvent): Boolean {

        /* Consume all touch events so they don't get dispatched to the view
     * beneath this view.
     */
        return true
    }

    fun show() {
        fade(VISIBLE, 0.0f, 1.0f)
    }

    fun hide() {
        fade(GONE, 1.0f, 0.0f)
    }

    private fun fade(visibility: Int, startAlpha: Float, endAlpha: Float) {
        val anim = AlphaAnimation(startAlpha, endAlpha)
        anim.duration = fadeTime.toLong()
        startAnimation(anim)
        setVisibility(visibility)
    }

    override fun hasFocus(): Boolean {
        val numChildren = childCount
        var hasFocus = false
        for (i in 0 until numChildren) {
            hasFocus = hasFocus || getChildAt(i).hasFocus()
        }
        return hasFocus
    }

    init {
        isFocusable = false
        val a = context.obtainStyledAttributes(attrs, R.styleable.ButtonLayerView)
        fadeTime = a.getResourceId(R.styleable.ButtonLayerView_fade_time, 500)
    }
}