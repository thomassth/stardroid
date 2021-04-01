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
import android.widget.CompoundButton;

/**
 * @author marcocipriani01
 */
public abstract class ImprovedToggleListener implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    boolean touched = false;

    @SuppressLint("ClickableViewAccessibility")
    public void attach(CompoundButton button) {
        button.setOnCheckedChangeListener(this);
        button.setOnTouchListener(this);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public final boolean onTouch(View v, MotionEvent event) {
        touched = true;
        return false;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (touched) {
            onImprovedCheckedChanged(buttonView, isChecked);
            touched = false;
        }
    }

    protected abstract void onImprovedCheckedChanged(CompoundButton buttonView, boolean isChecked);
}