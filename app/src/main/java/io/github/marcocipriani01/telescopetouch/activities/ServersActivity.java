/*
 * Copyright (C) 2020  Marco Cipriani (@marcocipriani01)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.github.marcocipriani01.telescopetouch.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.woxthebox.draglistview.DragListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.activities.util.ServersItemAdapter;
import io.github.marcocipriani01.telescopetouch.activities.util.ServersReloadListener;

/**
 * Activity to manage the list of servers.
 *
 * @author marcocipriani01
 */
public class ServersActivity extends AppCompatActivity implements ServersReloadListener {

    private final static Gson gson = new Gson();
    private final static Type STRING_ARRAY_TYPE = new TypeToken<ArrayList<String>>() {
    }.getType();
    private static AlertDialog lastDialog = null;
    private DragListView serversListView;
    private SharedPreferences preferences;
    private DarkerModeManager darkerModeManager;

    /**
     * Asks the user to add a new server.
     */
    @SuppressWarnings("SpellCheckingInspection")
    @SuppressLint("SetTextI18n")
    public static void addServer(final Context context, final ServersReloadListener onServersReload) {
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

        if (lastDialog != null)
            lastDialog.dismiss();
        lastDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.host_prompt_text).setView(layout).setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog12, id) -> {
                    inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    String server = input.getText().toString();
                    if (!server.equals("")) {
                        if (!isIp(server))
                            Toast.makeText(context, context.getString(R.string.not_valid_ip), Toast.LENGTH_SHORT).show();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        ArrayList<String> list = getServers(preferences);
                        list.add(0, server);
                        preferences.edit().putString(ApplicationConstants.INDI_SERVERS_PREF, gson.toJson(list, STRING_ARRAY_TYPE)).apply();
                        onServersReload.loadServers(list);
                    } else {
                        Toast.makeText(context, context.getString(R.string.empty_host), Toast.LENGTH_SHORT).show();
                    }
                    lastDialog = null;
                })
                .setIcon(R.drawable.edit)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> lastDialog = null).create();
        lastDialog.show();
        input.requestFocus();
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static ArrayList<String> getServers(SharedPreferences preferences) {
        String pref = preferences.getString(ApplicationConstants.INDI_SERVERS_PREF, null);
        if (pref == null) return new ArrayList<>();
        ArrayList<String> servers = null;
        try {
            servers = gson.fromJson(pref, STRING_ARRAY_TYPE);
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
        darkerModeManager = new DarkerModeManager(getWindow(), null, PreferenceManager.getDefaultSharedPreferences(this));
        setTheme(darkerModeManager.getPref() ? R.style.DarkerAppTheme : R.style.AppTheme);
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
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadServers(getServers(preferences));
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
    public void onResume() {
        super.onResume();
        darkerModeManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFromListView();
        darkerModeManager.stop();
    }

    private void saveFromListView() {
        preferences.edit().putString(ApplicationConstants.INDI_SERVERS_PREF,
                gson.toJson(((ServersItemAdapter) serversListView.getAdapter()).getItemList(), STRING_ARRAY_TYPE)).apply();
    }

    @Override
    public void loadServers(ArrayList<String> servers) {
        serversListView.setAdapter(new ServersItemAdapter(servers, R.layout.servers_list_item, R.id.listview_drag, false) {
            @Override
            public void onItemLongClicked(final TextView view) {
                new AlertDialog.Builder(ServersActivity.this)
                        .setTitle(R.string.host_manage)
                        .setIcon(R.drawable.delete)
                        .setMessage(R.string.remove_server)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                            List<String> list = getItemList();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i) == view.getText()) {
                                    removeItem(i);
                                    break;
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        }, false);
    }
}