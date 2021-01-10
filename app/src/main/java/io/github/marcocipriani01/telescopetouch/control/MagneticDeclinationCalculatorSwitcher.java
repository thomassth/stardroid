package io.github.marcocipriani01.telescopetouch.control;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Named;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;

/**
 * Aggregates the RealMagneticDeclinationCalculator and the
 * ZeroMagneticDeclinationCalculator and switches them in the AstronomerModel.
 *
 * @author John Taylor
 */
public class MagneticDeclinationCalculatorSwitcher implements OnSharedPreferenceChangeListener {
    private static final String KEY = "use_magnetic_correction";
    private static final String TAG = TelescopeTouchApplication.getTag(MagneticDeclinationCalculatorSwitcher.class);

    private final MagneticDeclinationCalculator realCalculator;
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
    public MagneticDeclinationCalculatorSwitcher(
            AstronomerModel model,
            SharedPreferences preferences,
            @Named("zero") MagneticDeclinationCalculator zeroCalculator,
            @Named("real") MagneticDeclinationCalculator realCalculator) {
        this.zeroCalculator = zeroCalculator;
        this.realCalculator = realCalculator;
        this.model = model;
        preferences.registerOnSharedPreferenceChangeListener(this);
        setTheModelsCalculator(preferences);
    }

    private void setTheModelsCalculator(SharedPreferences preferences) {
        boolean useRealCalculator = preferences.getBoolean(KEY, true);
        if (useRealCalculator) {
            model.setMagneticDeclinationCalculator(realCalculator);
        } else {
            model.setMagneticDeclinationCalculator(zeroCalculator);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO(johntaylor): investigate the preferences API - currently we have too
        // many classes all hanging off SharedPreferences.
        if (KEY.equals(key)) {
            Log.i(TAG, "Magnetic declination preference changed");
            setTheModelsCalculator(sharedPreferences);
        }

    }
}