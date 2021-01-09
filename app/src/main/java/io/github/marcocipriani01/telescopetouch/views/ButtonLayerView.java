package io.github.marcocipriani01.telescopetouch.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * Contains the provider buttons.
 */

public class ButtonLayerView extends LinearLayout {

    public ButtonLayerView(Context context) {
        this(context, null);
    }

    public ButtonLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonLayerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /* Consume all touch events so they don't get dispatched to the view
         * beneath this view.
         */
        return true;
    }

    public void show() {
        setVisibility(View.GONE);
    }

    public void hide() {
        setVisibility(View.GONE);
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
