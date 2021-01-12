package io.github.marcocipriani01.telescopetouch.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.woxthebox.draglistview.DragListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.views.ServersItemAdapter;

/**
 * @author marcocipriani01
 */
interface ServersReloadListener {
    void loadServers(ArrayList<String> servers);
}

/**
 * Activity to manage the list of servers.
 *
 * @author marcocipriani01
 */
public class ServersActivity extends AppCompatActivity implements ServersReloadListener {

    public static final String INDI_SERVERS_PREF = "INDI_SERVERS_PREF";
    final static Gson gson = new Gson();
    final static Type stringArrayType = new TypeToken<ArrayList<String>>() {
    }.getType();
    private DragListView serversListView;

    /**
     * Asks the user to add a new server.
     */
    @SuppressWarnings("SpellCheckingInspection")
    @SuppressLint("SetTextI18n")
    static void addServer(final Context context, final ServersReloadListener onServersReload) {
        final EditText input = new EditText(context);
        input.setText("192.168.");
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

        new AlertDialog.Builder(context)
                .setTitle(R.string.host_prompt_text).setView(layout).setCancelable(false)
                .setPositiveButton(context.getString(android.R.string.ok), (dialog12, id) -> {
                    inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    String server = input.getText().toString();
                    if (!server.equals("")) {
                        if (!isIp(server))
                            Toast.makeText(context, context.getString(R.string.not_valid_ip), Toast.LENGTH_SHORT).show();
                        // Retrieve the list
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        ArrayList<String> list = getServers(preferences);
                        list.add(0, server);
                        // Save the list
                        preferences.edit().putString(INDI_SERVERS_PREF, gson.toJson(list, stringArrayType)).apply();
                        // Update
                        onServersReload.loadServers(list);
                    } else {
                        Toast.makeText(context, context.getString(R.string.empty_host), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), null).show();
        input.requestFocus();
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static ArrayList<String> getServers(Context context) {
        return getServers(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static ArrayList<String> getServers(SharedPreferences preferences) {
        String pref = preferences.getString(INDI_SERVERS_PREF, null);
        if (pref == null) return new ArrayList<>();
        ArrayList<String> servers = null;
        try {
            servers = gson.fromJson(pref, stringArrayType);
        } catch (Exception e) {
            Log.e("ServersActivity", "Gson error.", e);
        }
        if (servers == null) servers = new ArrayList<>();
        return servers;
    }

    private static boolean isIp(final String ip) {
        return ip.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        findViewById(R.id.addServerFab).setOnClickListener(v -> {
            saveFromListView();
            addServer(ServersActivity.this, ServersActivity.this);
        });
        serversListView = findViewById(R.id.serversList);
        serversListView.setLayoutManager(new LinearLayoutManager(this));
        serversListView.setCanDragHorizontally(false);
        loadServers(getServers(this));
        Toast.makeText(this, R.string.servers_list_toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFromListView();
    }

    private void saveFromListView() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                INDI_SERVERS_PREF,
                gson.toJson(((ServersItemAdapter) serversListView.getAdapter()).getItemList(), stringArrayType))
                .apply();
    }

    @Override
    public void loadServers(ArrayList<String> servers) {
        serversListView.setAdapter(new ServersItemAdapter(servers, R.layout.servers_list_item, R.id.listview_drag, false) {
            @Override
            public void onItemLongClicked(final TextView view) {
                new AlertDialog.Builder(ServersActivity.this)
                        .setTitle(R.string.sure)
                        .setMessage(R.string.remove_server)
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok), (dialog, id) -> {
                            List<String> list = getItemList();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i) == view.getText()) {
                                    removeItem(i);
                                    break;
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null).show();
            }
        }, false);
    }
}