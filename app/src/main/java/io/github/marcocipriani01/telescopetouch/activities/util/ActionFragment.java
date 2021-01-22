package io.github.marcocipriani01.telescopetouch.activities.util;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public abstract class ActionFragment extends Fragment implements Runnable {

    protected Context context;
    private volatile ActionListener listener = null;

    public void setActionEnabledListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public abstract boolean isActionEnabled();

    protected void notifyActionChange() {
        if (listener != null) listener.setActionEnabled(isActionEnabled());
    }

    protected void requestActionSnack(int msgRes) {
        if (listener != null) listener.actionSnackRequested(msgRes);
    }

    protected void requestActionSnack(int msgRes, int actionName, View.OnClickListener action) {
        if (listener != null) listener.actionSnackRequested(msgRes, actionName, action);
    }

    public abstract int getActionDrawable();

    public interface ActionListener {
        void setActionEnabled(boolean actionEnabled);

        void actionSnackRequested(int msgRes);

        void actionSnackRequested(int msgRes, int actionName, View.OnClickListener action);
    }
}