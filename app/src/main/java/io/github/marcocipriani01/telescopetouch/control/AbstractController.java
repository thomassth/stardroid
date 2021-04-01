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

package io.github.marcocipriani01.telescopetouch.control;

import android.util.Log;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Implements some of the boilerplate of a {@link Controller}.
 *
 * @author John Taylor
 */
public abstract class AbstractController implements Controller {

    private static final String TAG = TelescopeTouchApp.getTag(AbstractController.class);
    protected boolean enabled = true;
    @Inject
    AstronomerModel model;

    @Override
    public void setModel(AstronomerModel model) {
        this.model = model;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            Log.d(TAG, "Enabling controller " + this);
        } else {
            Log.d(TAG, "Disabling controller " + this);
        }
        this.enabled = enabled;
    }
}