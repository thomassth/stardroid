package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * No sensors dialog fragment.
 * Created by johntaylor on 4/9/16.
 */
public class NoSensorsDialogFragment extends DialogFragment {
    private static final String TAG = MiscUtil.getTag(NoSensorsDialogFragment.class);
    @Inject
    Activity parentActivity;
    @Inject
    SharedPreferences preferences;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);

        LayoutInflater inflater = parentActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.no_sensor_warning, null);
        return new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.warning_dialog_title)
                .setView(view).setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.d(TAG, "No Sensor Dialog closed");
                                preferences.edit().putBoolean(
                                        ApplicationConstants.NO_WARN_ABOUT_MISSING_SENSORS,
                                        ((CheckBox) view.findViewById(R.id.no_show_dialog_again)).isChecked()).commit();
                                dialog.dismiss();
                            }
                        }).create();
    }

    public interface ActivityComponent {
        void inject(NoSensorsDialogFragment fragment);
    }
}
