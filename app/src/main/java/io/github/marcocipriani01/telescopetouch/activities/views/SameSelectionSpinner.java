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

package io.github.marcocipriani01.telescopetouch.activities.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSpinner;

public class SameSelectionSpinner extends AppCompatSpinner {

    public SameSelectionSpinner(Context context) {
        super(context);
    }

    public SameSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SameSelectionSpinner(Context context, AttributeSet attrs, int defStyle) {
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