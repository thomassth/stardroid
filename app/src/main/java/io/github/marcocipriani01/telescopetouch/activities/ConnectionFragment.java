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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.util.NSDHelper;
import io.github.marcocipriani01.telescopetouch.views.ImprovedSpinner;
import io.github.marcocipriani01.telescopetouch.views.ImprovedSpinnerListener;

/**
 * The main screen of the application, which manages the connection.
 *
 * @author Romain Fafet
 * @author marcocipriani01
 */
public class ConnectionFragment extends Fragment implements ServersReloadListener,
        ConnectionManager.UIUpdater, NSDHelper.NSDListener {

    private static final String INDI_PORT_PREF = "INDI_PORT_PREF";
    private static final String NSD_PREF = "NSD_PREF";
    /**
     * The last position of the spinner (to restore the Fragment's state)
     */
    private static int selectedSpinnerItem = 0;
    private ConnectionManager connectionManager;
    private SharedPreferences preferences;
    private Context context;
    private Button connectionButton;
    private ImprovedSpinner serversSpinner;
    private final ImprovedSpinnerListener spinnerListener = new ImprovedSpinnerListener() {
        @Override
        protected void onImprovedItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            if (selected.equals(getResources().getString(R.string.host_add))) {
                serversSpinner.post(() -> serversSpinner.setSelection(0));
                ServersActivity.addServer(context, ConnectionFragment.this);
            } else if (selected.equals(getResources().getString(R.string.host_manage))) {
                startActivityForResult(new Intent(context, ServersActivity.class), 1);
            }
        }
    };
    private NSDHelper nsdHelper;
    private EditText portEditText;
    /**
     * The original position of the floating action button.
     */
    private int fabPosY;
    private FloatingActionButton clearLogsButton;
    private ListView logsList;
    private LogAdapter logAdapter;
    private boolean showNsd = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        setHasOptionsMenu(true);
        connectionManager = TelescopeTouchApp.getConnectionManager();
        logsList = rootView.findViewById(R.id.logsList);
        logAdapter = new LogAdapter(context);
        logAdapter.addAll(connectionManager.getLogs());
        logsList.setAdapter(logAdapter);
        clearLogsButton = rootView.findViewById(R.id.clearLogsButton);
        clearLogsButton.setOnClickListener(v -> {
            connectionManager.clearLogs();
            logAdapter.clear();
            logAdapter.notifyDataSetChanged();
            clearLogsButton.animate().translationY(250);
        });
        fabPosY = clearLogsButton.getScrollY();
        if (connectionManager.getLogs().size() == 0)
            clearLogsButton.animate().setDuration(0).translationXBy(250);
        logsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                clearLogsButton.animate().cancel();
                if (firstVisibleItem >= visibleItemCount) {
                    clearLogsButton.animate().translationYBy(250);
                } else {
                    clearLogsButton.animate().translationY(fabPosY);
                }
            }
        });

        connectionButton = rootView.findViewById(R.id.connectionButton);
        serversSpinner = rootView.findViewById(R.id.spinnerHost);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        showNsd = preferences.getBoolean(NSD_PREF, true);
        nsdHelper = TelescopeTouchApp.getServiceDiscoveryHelper();
        loadServers(ServersActivity.getServers(preferences));
        portEditText = rootView.findViewById(R.id.port_edittext);
        portEditText.setText(String.valueOf(preferences.getInt(INDI_PORT_PREF, 7624)));
        connectionButton.setOnClickListener(v -> {
            String host = String.valueOf(serversSpinner.getSelectedItem());
            if (host.contains("@")) {
                String[] split = host.split("@");
                if (split.length == 2) host = split[1];
            }
            String portStr = portEditText.getText().toString();
            int port;
            if (portStr.equals("")) {
                port = 7624;
            } else {
                try {
                    port = Integer.parseInt(portStr);
                    preferences.edit().putInt(INDI_PORT_PREF, port).apply();
                } catch (NumberFormatException e) {
                    port = 7624;
                }
            }
            ConnectionManager.ConnectionState state = connectionManager.getState();
            if (state == ConnectionManager.ConnectionState.DISCONNECTED) {
                if (host.equals(getResources().getString(R.string.host_add))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    ServersActivity.addServer(context, ConnectionFragment.this);
                } else if (host.equals(getResources().getString(R.string.host_manage))) {
                    startActivityForResult(new Intent(context, ServersActivity.class), 1);
                } else {
                    connectionManager.connect(host, port);
                }
            } else if (state == ConnectionManager.ConnectionState.CONNECTED) {
                connectionManager.disconnect();
            }
            ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(portEditText.getWindowToken(), 0);
        });
        serversSpinner.setSelection(selectedSpinnerItem);
        spinnerListener.attach(serversSpinner);

        refreshUi(connectionManager.getState());
        connectionManager.setUiUpdater(this);

        nsdHelper.setListener(this);
        if (!nsdHelper.isAvailable()) connectionManager.log("NSD not available.");
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.nsd, menu);
        MenuItem item = menu.findItem(R.id.menu_nsd);
        item.setEnabled(nsdHelper.isAvailable());
        item.setChecked(showNsd);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_nsd) {
            showNsd = !item.isChecked();
            item.setChecked(showNsd);
            loadServers(ServersActivity.getServers(preferences));
            preferences.edit().putBoolean(NSD_PREF, showNsd).apply();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        selectedSpinnerItem = serversSpinner.getSelectedItemPosition();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) loadServers(ServersActivity.getServers(context));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nsdHelper.setListener(null);
    }

    @Override
    public void updateConnectionState(ConnectionManager.ConnectionState state) {
        new Handler(Looper.getMainLooper()).post(() -> refreshUi(state));
    }

    @Override
    public void addLog(final ConnectionManager.LogItem log) {
        if (logsList != null) {
            logsList.post(() -> {
                logAdapter.add(log);
                logAdapter.notifyDataSetChanged();
                if (connectionManager.getLogs().size() == 1) {
                    clearLogsButton.animate().cancel();
                    clearLogsButton.animate().translationY(fabPosY);
                }
            });
        }
    }

    private void refreshUi(ConnectionManager.ConnectionState state) {
        switch (state) {
            case CONNECTED: {
                connectionButton.post(() -> {
                    connectionButton.setText(getString(R.string.disconnect));
                    connectionButton.setEnabled(true);
                });
                serversSpinner.post(() -> serversSpinner.setEnabled(false));
                portEditText.post(() -> portEditText.setEnabled(false));
                break;
            }
            case DISCONNECTED: {
                connectionButton.post(() -> {
                    connectionButton.setText(getString(R.string.connect));
                    connectionButton.setEnabled(true);
                });
                serversSpinner.post(() -> serversSpinner.setEnabled(true));
                portEditText.post(() -> portEditText.setEnabled(true));
                break;
            }
            case BUSY: {
                connectionButton.post(() -> {
                    connectionButton.setText(getString(R.string.connecting));
                    connectionButton.setEnabled(false);
                });
                serversSpinner.post(() -> serversSpinner.setEnabled(false));
                portEditText.post(() -> portEditText.setEnabled(false));
                break;
            }
        }
    }

    @Override
    public void loadServers(ArrayList<String> servers) {
        Resources resources = context.getResources();
        if (showNsd) {
            HashMap<String, String> services = nsdHelper.getDiscoveredServices();
            for (String name : services.keySet()) {
                String ip = services.get(name);
                if (ip != null) servers.add(name.replace("@", "") + "@" + ip);
            }
        }
        servers.add(resources.getString(R.string.host_add));
        servers.add(resources.getString(R.string.host_manage));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, servers);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serversSpinner.setAdapter(dataAdapter);
    }

    @Override
    public void onNSDChange() {
        new Handler(Looper.getMainLooper()).post(() -> loadServers(ServersActivity.getServers(preferences)));
    }

    /**
     * {@code ArrayAdapter} for logs.
     *
     * @author marcocipriani01
     */
    private static class LogAdapter extends ArrayAdapter<ConnectionManager.LogItem> {

        private final LayoutInflater inflater;

        private LogAdapter(Context context) {
            super(context, R.layout.logs_item);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.logs_item, parent, false);
                holder = new ViewHolder();
                holder.log = convertView.findViewById(R.id.logs_item1);
                holder.timestamp = convertView.findViewById(R.id.logs_item2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ConnectionManager.LogItem item = getItem(position);
            if (item != null) {
                holder.log.setText(item.getLog());
                holder.timestamp.setText(item.getTimestamp());
            }
            return convertView;
        }

        /**
         * {@code ViewHolder} for the {@code ListView} that stores logs.
         *
         * @author marcocipriani01
         */
        private static class ViewHolder {
            TextView log, timestamp;
        }
    }
}