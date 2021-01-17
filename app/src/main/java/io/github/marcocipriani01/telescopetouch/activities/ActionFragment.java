package io.github.marcocipriani01.telescopetouch.activities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public abstract class ActionFragment extends Fragment implements Runnable {

    protected Context context;
    private volatile ActionFragmentListener listener = null;
    private boolean actionEnabled = false;

    public void setActionEnabledListener(ActionFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public boolean isActionEnabled() {
        return actionEnabled;
    }

    protected void setActionEnabled(boolean actionEnabled) {
        this.actionEnabled = actionEnabled;
        if (listener != null) listener.setActionEnabled(actionEnabled);
    }

    public abstract int getActionDrawable();

    public interface ActionFragmentListener {
        void setActionEnabled(boolean actionEnabled);
    }
}