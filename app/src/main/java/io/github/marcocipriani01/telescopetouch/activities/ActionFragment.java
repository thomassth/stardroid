package io.github.marcocipriani01.telescopetouch.activities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public abstract class ActionFragment extends Fragment implements Runnable {

    protected Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public abstract int getActionDrawable();
}