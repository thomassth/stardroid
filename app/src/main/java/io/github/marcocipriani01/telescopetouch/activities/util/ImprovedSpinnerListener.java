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

package io.github.marcocipriani01.telescopetouch.activities.util;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * @author marcocipriani01
 */
public abstract class ImprovedSpinnerListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private boolean touched = false;

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