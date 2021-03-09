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
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import org.indilib.i4j.client.INDILightElement;
import org.indilib.i4j.client.INDIProperty;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;

public class LightPropPref extends PropPref<INDILightElement> {

    public LightPropPref(Context context, INDIProperty<INDILightElement> prop) {
        super(context, prop);
    }

    /**
     * Create the summary rich-text string
     *
     * @return the summary
     */
    @Override
    protected Spannable createSummary() {
        List<INDILightElement> elements = prop.getElementsAsList();
        int count = elements.size();
        if (count > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            int[] starts = new int[count];
            int[] ends = new int[count];
            starts[0] = 0;
            for (int i = 0; i < count; i++) {
                starts[i] = stringBuilder.length();
                stringBuilder.append(elements.get(i).getLabel()).append(" ");
                ends[i] = stringBuilder.length();
            }
            Spannable summaryText = new SpannableString(stringBuilder.toString());
            for (int i = 0; i < count; i++) {
                int color;
                switch (elements.get(i).getValue()) {
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
                    default:
                    case IDLE: {
                        color = Color.WHITE;
                        break;
                    }
                }
                summaryText.setSpan(new ForegroundColorSpan(color), starts[i], ends[i], 0);
            }
            return summaryText;
        } else {
            return new SpannableString(getContext().getString(R.string.no_indi_elements));
        }
    }
}