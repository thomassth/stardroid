package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * A dialog fragment that only shows itself if it's not already shown.  This prevents
 * a java.lang.IllegalStateException when the activity gets backgrounded.
 * Created by johntaylor on 4/11/16.
 */
public abstract class ImprovedDialogFragment extends DialogFragment {
    @Override
    public void show(@NonNull FragmentManager fragmentManager, String tag) {
        if (this.isAdded()) return;
        super.show(fragmentManager, tag);
    }
}