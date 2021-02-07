package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputFilter;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.ServersActivity;

import static io.github.marcocipriani01.telescopetouch.activities.ServersActivity.getServers;

public abstract class NewServerDialog {

    private static AlertDialog lastDialog = null;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("SpellCheckingInspection")
    public NewServerDialog(Context context, SharedPreferences preferences) {
        final EditText input = new EditText(context);
        input.setText("192.168.");
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setHint(context.getString(R.string.ip_address));
        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend);
                if (!resultingTxt
                        .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.parseInt(split) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };
        input.setFilters(filters);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
        layoutParams.setMargins(padding, 0, padding, 0);
        layout.addView(input, layoutParams);
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (lastDialog != null)
            lastDialog.dismiss();
        lastDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.host_prompt_text).setView(layout).setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog12, id) -> {
                    inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    String server = input.getText().toString();
                    if (!server.equals("")) {
                        if (!isIp(server))
                            createSnackbar(R.string.not_valid_ip);
                        ArrayList<String> list = getServers(preferences);
                        list.add(0, server);
                        ServersActivity.saveServers(preferences, list);
                        loadServers(list);
                    } else {
                        createSnackbar(R.string.empty_host);
                    }
                    lastDialog = null;
                })
                .setIcon(R.drawable.edit)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> lastDialog = null).create();
        lastDialog.show();
        input.requestFocus();
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private static boolean isIp(final String ip) {
        return ip.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }

    protected abstract void createSnackbar(int msg);

    protected abstract void loadServers(ArrayList<String> servers);
}