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