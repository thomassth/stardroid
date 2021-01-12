package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;

/**
 * End User License agreement dialog.
 * Created by johntaylor on 4/3/16.
 */
public class NoSearchResultsDialogFragment extends DialogFragment {

    private static final String TAG = TelescopeTouchApp.getTag(NoSearchResultsDialogFragment.class);
    @Inject
    Activity parentActivity;

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) requireActivity()).getComponent().inject(this);

        return new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.no_search_title).setMessage(R.string.no_search_results_text)
                .setNegativeButton(android.R.string.ok, null).create();
    }

    public interface ActivityComponent {
        void inject(NoSearchResultsDialogFragment fragment);
    }
}