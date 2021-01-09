package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.activities.DynamicStarMapActivity;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Time travel dialog fragment.
 * Created by johntaylor on 4/3/16.
 */
// TODO(jontayler): see if this crashes when backgrounded on older devices and use
// the fragment in this package if so.
public class TimeTravelDialogFragment extends android.app.DialogFragment {
    private static final String TAG = MiscUtil.getTag(TimeTravelDialogFragment.class);
    @Inject
    DynamicStarMapActivity parentActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);

        return new TimeTravelDialog(parentActivity,
                parentActivity.getModel());
    }

    public interface ActivityComponent {
        void inject(TimeTravelDialogFragment fragment);
    }
}
