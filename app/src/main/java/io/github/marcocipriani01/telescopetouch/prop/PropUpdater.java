/*
 * Copyright (C) 2020  Marco Cipriani (@marcocipriani01)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.github.marcocipriani01.telescopetouch.prop;

import android.util.Log;

import org.indilib.i4j.client.INDIProperty;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Thread to send updates to the server
 */
public class PropUpdater extends Thread {

    public PropUpdater(INDIProperty<?> prop) {
        super(() -> {
            try {
                prop.sendChangesToDriver();
            } catch (Exception e) {
                Log.e("PropertyUpdater", "Property update error!", e);
                TelescopeTouchApp.log(TelescopeTouchApp.getContext().getResources().getString(R.string.error) + " " + e.getLocalizedMessage());
            }
        }, "INDI property updater");
    }
}