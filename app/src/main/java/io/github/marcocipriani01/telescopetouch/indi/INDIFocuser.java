package io.github.marcocipriani01.telescopetouch.indi;

import android.content.Context;
import android.os.Handler;

import org.indilib.i4j.client.INDIDevice;

import java.util.HashSet;
import java.util.Set;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class INDIFocuser {

    private static final String TAG = TelescopeTouchApp.getTag(INDIFocuser.class);
    public final INDIDevice device;
    private final Context context;
    private final Handler uiHandler;
    private final Set<FocuserListener> listeners = new HashSet<>();

    public INDIFocuser(INDIDevice device, Context context, Handler uiHandler) {
        this.device = device;
        this.context = context;
        this.uiHandler = uiHandler;
    }

    public interface FocuserListener {
        void onPositionChange(int position);
    }
}