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
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDILightProperty;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDITextProperty;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public abstract class PropPref<Element extends INDIElement> extends Preference implements INDIPropertyListener {

    private static final String TAG = TelescopeTouchApp.getTag(PropPref.class);
    protected final INDIProperty<Element> prop;
    protected final Resources resources;
    protected View title = null;

    protected PropPref(Context context, INDIProperty<Element> prop) {
        super(context);
        this.resources = context.getResources();
        this.prop = prop;
        prop.addINDIPropertyListener(this);
        setTitle(createTitle());
        setSummary(createSummary());
    }

    public static PropPref<?> create(Context context, INDIProperty<?> prop) {
        try {
            if (prop instanceof INDISwitchProperty) {
                return new SwitchPropPref(context, (INDISwitchProperty) prop);
            } else if (prop instanceof INDILightProperty) {
                return new LightPropPref(context, (INDILightProperty) prop);
            } else if (prop instanceof INDITextProperty) {
                return new TextPropPref(context, (INDITextProperty) prop);
            } else if (prop instanceof INDINumberProperty) {
                return new NumberPropPref(context, (INDINumberProperty) prop);
            } else if (prop instanceof INDIBLOBProperty) {
                return new BLOBPropPref(context, (INDIBLOBProperty) prop);
            } else {
                Log.w(TAG, "INDI property \"" + prop.toString() + "\" couldn't be added, unknown type.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public INDIProperty<Element> getProp() {
        return prop;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        title = holder.itemView;
    }

    /**
     * Create the title rich-text string with the color corresponding to the
     * Property state
     *
     * @return the title
     */
    protected Spannable createTitle() {
        Spannable titleText = new SpannableString(prop.getLabel());
        int color;
        switch (prop.getState()) {
            case ALERT: {
                color = resources.getColor(R.color.light_red);
                break;
            }
            case BUSY: {
                color = resources.getColor(R.color.light_yellow);
                break;
            }
            case OK: {
                color = resources.getColor(R.color.light_green);
                break;
            }
            default: {
                color = Color.WHITE;
                break;
            }
        }
        titleText.setSpan(new ForegroundColorSpan(color), 0, titleText.length(), 0);
        return titleText;
    }

    /**
     * Create the summary rich-text string
     *
     * @return the summary
     */
    protected abstract Spannable createSummary();

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        if (property != prop) {
            Log.w(TAG, "Wrong property updated");
            return;
        }
        if (title != null) title.post(() -> {
            try {
                PropPref.this.setSummary(createSummary());
                PropPref.this.setTitle(createTitle());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }
}