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