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

package io.github.marcocipriani01.telescopetouch.indi;

import android.util.Log;

import org.indilib.i4j.client.INDIProperty;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Thread to send updates to the server
 */
public class PropUpdater extends Thread {

    private static final String TAG = TelescopeTouchApp.getTag(PropUpdater.class);
    private final INDIProperty<?> prop;

    public PropUpdater(INDIProperty<?> prop) {
        super("INDI property updater");
        this.prop = prop;
    }

    @Override
    public void run() {
        if (prop == null) {
            Log.i(TAG, "INDI property is null, aborting");
            return;
        }
        try {
            prop.sendChangesToDriver();
        } catch (Exception e) {
            Log.e(TAG, "Property update error!", e);
            TelescopeTouchApp.connectionManager.log(
                    TelescopeTouchApp.getContext().getString(R.string.error) + " " + e.getLocalizedMessage());
        }
    }
}