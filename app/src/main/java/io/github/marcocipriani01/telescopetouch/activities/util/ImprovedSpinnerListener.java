/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
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