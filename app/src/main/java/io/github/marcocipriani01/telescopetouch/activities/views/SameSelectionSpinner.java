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