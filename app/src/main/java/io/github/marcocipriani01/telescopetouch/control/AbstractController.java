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

    // TODO(jontayler): remove this
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