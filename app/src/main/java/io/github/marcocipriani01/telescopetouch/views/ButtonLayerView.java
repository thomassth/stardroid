package io.github.marcocipriani01.telescopetouch.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * Contains the provider buttons.
 */

public class ButtonLayerView extends LinearLayout {

    private final int fadeTime;

    public ButtonLayerView(Context context) {
        this(context, null);
    }

    public ButtonLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ButtonLayerView);
        fadeTime = attributes.getResourceId(R.styleable.ButtonLayerView_fade_time, 500);
        attributes.recycle();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            fade(View.VISIBLE, 0.0f, 1.0f);
        } else {
            fade(View.GONE, 1.0f, 0.0f);
        }
    }

    private void fade(int visibility, float startAlpha, float endAlpha) {
        AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(fadeTime);
        startAnimation(anim);
        super.setVisibility(visibility);
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