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

package io.github.marcocipriani01.telescopetouch.indi;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIProperty;

import io.github.marcocipriani01.telescopetouch.R;

public class UnavailablePropPref<Element extends INDIElement> extends PropPref<Element> {

    private final Exception error;

    protected UnavailablePropPref(Context context, INDIProperty<Element> prop) {
        super(context, prop);
        error = null;
    }

    protected UnavailablePropPref(Context context, INDIProperty<Element> prop, Exception error) {
        super(context, prop);
        this.error = error;
    }

    @Override
    protected Spannable createTitle() {
        Spannable titleText = new SpannableString(prop.getLabel());
        titleText.setSpan(new ForegroundColorSpan(resources.getColor(R.color.light_red)), 0, titleText.length(), 0);
        return titleText;
    }

    @Override
    protected Spannable createSummary() {
        return (error == null) ? new SpannableString(resources.getString(R.string.prop_unavailable)) :
                new SpannableString(error.getLocalizedMessage());
    }
}