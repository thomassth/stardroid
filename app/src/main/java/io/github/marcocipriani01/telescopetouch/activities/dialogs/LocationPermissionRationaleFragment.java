package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * Dialog explaining the need for the auto-location permission.
 * Created by johntaylor on 4/3/16.
 */
public class LocationPermissionRationaleFragment extends ImprovedDialogFragment implements Dialog.OnClickListener {

    private Callback resultListener;

    public LocationPermissionRationaleFragment() {
    }

    public void setCallback(Callback resultListener) {
        this.resultListener = resultListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.location_rationale_title)
                .setMessage(R.string.location_rationale_text)
                .setNeutralButton(R.string.dialog_ok_button, LocationPermissionRationaleFragment.this);
        return dialogBuilder.create();
    }

    @Override
    public void onClick(DialogInterface ignore1, int ignore2) {
        if (resultListener != null) {
            resultListener.done();
        }
    }

    public interface Callback {
        void done();
    }
}