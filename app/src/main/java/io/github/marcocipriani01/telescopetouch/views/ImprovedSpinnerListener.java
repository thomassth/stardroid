package io.github.marcocipriani01.telescopetouch.views;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * @author marcocipriani01
 */
public abstract class ImprovedSpinnerListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    boolean touched = false;

    public void attach(Spinner spinner) {
        spinner.setOnItemSelectedListener(this);
        spinner.setOnTouchListener(this);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public final boolean onTouch(View v, MotionEvent event) {
        touched = true;
        return false;
    }

    @Override
    public final void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (touched) {
            onImprovedItemSelected(parent, view, pos, id);
            touched = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    protected abstract void onImprovedItemSelected(AdapterView<?> parent, View view, int pos, long id);
}