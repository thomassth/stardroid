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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Named;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Aggregates the RealMagneticDeclinationCalculator and the
 * ZeroMagneticDeclinationCalculator and switches them in the AstronomerModel.
 *
 * @author John Taylor
 */
public class MagneticDeclinationSwitcher implements OnSharedPreferenceChangeListener {

    private static final String TAG = TelescopeTouchApp.getTag(MagneticDeclinationSwitcher.class);
    private final MagneticDeclinationCalculator realCalculator;
    private final SharedPreferences preferences;
    private final MagneticDeclinationCalculator zeroCalculator;
    private final AstronomerModel model;

    /**
     * Constructs a new MagneticDeclinationCalculatorSwitcher.
     *
     * @param model       the object in which to swap the calculator
     * @param preferences a SharedPreferences object which will indicate which
     *                    calculator to use.
     */
    @Inject
    public MagneticDeclinationSwitcher(AstronomerModel model, SharedPreferences preferences,
                                       @Named("zero") MagneticDeclinationCalculator zeroCalculator,
                                       @Named("real") MagneticDeclinationCalculator realCalculator) {
        this.preferences = preferences;
        this.zeroCalculator = zeroCalculator;
        this.realCalculator = realCalculator;
        this.model = model;
    }

    public void init() {
        preferences.registerOnSharedPreferenceChangeListener(this);
        setTheModelsCalculator(preferences);
    }

    private void setTheModelsCalculator(SharedPreferences preferences) {
        if (preferences.getBoolean(ApplicationConstants.MAGNETIC_DECLINATION_PREF, true)) {
            model.setMagneticDeclinationCalculator(realCalculator);
        } else {
            model.setMagneticDeclinationCalculator(zeroCalculator);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (ApplicationConstants.MAGNETIC_DECLINATION_PREF.equals(key)) {
            Log.i(TAG, "Magnetic declination preference changed");
            setTheModelsCalculator(sharedPreferences);
        }
    }
}