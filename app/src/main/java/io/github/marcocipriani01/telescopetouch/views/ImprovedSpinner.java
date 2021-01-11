package io.github.marcocipriani01.telescopetouch.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSpinner;

public class ImprovedSpinner extends AppCompatSpinner {

    public ImprovedSpinner(Context context) {
        super(context);
    }

    public ImprovedSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImprovedSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = (position == getSelectedItemPosition());
        super.setSelection(position, animate);
        callListener(sameSelected, position);
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = (position == getSelectedItemPosition());
        super.setSelection(position);
        callListener(sameSelected, position);
    }

    private void callListener(boolean sameSelected, int position) {
        if (sameSelected) {
            OnItemSelectedListener listener = getOnItemSelectedListener();
            if (listener != null)
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}