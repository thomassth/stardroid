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

package io.github.marcocipriani01.telescopetouch.activities.views;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * Contains the provider buttons.
 */

public class FloatingButtonsLayout extends LinearLayout {

    private final int fadeTime;
    private final float distance;
    private final boolean invertDirection;

    public FloatingButtonsLayout(Context context) {
        this(context, null);
    }

    public FloatingButtonsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FloatingButtonsLayout);
        fadeTime = attributes.getInt(R.styleable.FloatingButtonsLayout_fade_time, 500);
        distance = attributes.getFloat(R.styleable.FloatingButtonsLayout_distance, 200f);
        invertDirection = attributes.getBoolean(R.styleable.FloatingButtonsLayout_invert_direction, false);
        attributes.recycle();
        setClipChildren(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void setVisibility(int visibility) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(this, "translationX",
                (visibility == VISIBLE) ? 0f : (invertDirection ? (-distance) : distance));
        animation.setDuration(fadeTime);
        animation.start();
    }

    @Override
    public boolean hasFocus() {
        int numChildren = getChildCount();
        boolean hasFocus = false;
        for (int i = 0; i < numChildren; ++i) {
            hasFocus = hasFocus || getChildAt(i).hasFocus();
        }
        return hasFocus;
    }
}