package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;

/**
 * End User License agreement dialog.
 * Created by johntaylor on 4/3/16.
 */
public class EulaDialogFragment extends ImprovedDialogFragment {

    private static final String TAG = TelescopeTouchApplication.getTag(EulaDialogFragment.class);
    @Inject
    Activity parentActivity;
    private EulaAcceptanceListener resultListener;

    public void setEulaAcceptanceListener(EulaAcceptanceListener resultListener) {
        this.resultListener = resultListener;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) Objects.requireNonNull(getActivity())).getComponent().inject(this);

        LayoutInflater inflater = parentActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.tos_view, null);

        String eulaText = parentActivity.getString(R.string.eula_text);
        Spanned formattedEulaText = Html.fromHtml(eulaText);
        TextView eulaTextView = view.findViewById(R.id.eula_box_text);
        eulaTextView.setText(formattedEulaText, TextView.BufferType.SPANNABLE);

        AlertDialog.Builder tosDialogBuilder = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.menu_tos)
                .setView(view);
        if (resultListener != null) {
            tosDialogBuilder
                    .setPositiveButton(R.string.dialog_accept, (dialog, whichButton) -> acceptEula(dialog))
                    .setNegativeButton(R.string.dialog_decline, (dialog, whichButton) -> rejectEula(dialog));
        }
        return tosDialogBuilder.create();
    }

    private void acceptEula(DialogInterface dialog) {
        Log.d(TAG, "TOS Dialog closed.  User accepts.");
        dialog.dismiss();
        if (resultListener != null) {
            resultListener.eulaAccepted();
        }
    }

    private void rejectEula(DialogInterface dialog) {
        Log.d(TAG, "TOS Dialog closed.  User declines.");
        dialog.dismiss();
        if (resultListener != null) {
            resultListener.eulaRejected();
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        rejectEula(dialog);
    }

    public interface EulaAcceptanceListener {
        void eulaAccepted();

        void eulaRejected();
    }

    public interface ActivityComponent {
        void inject(EulaDialogFragment fragment);
    }
}