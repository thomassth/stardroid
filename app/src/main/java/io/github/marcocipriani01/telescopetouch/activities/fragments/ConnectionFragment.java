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

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.MainActivity;
import io.github.marcocipriani01.telescopetouch.activities.ServersActivity;
import io.github.marcocipriani01.telescopetouch.activities.util.ImprovedSpinnerListener;
import io.github.marcocipriani01.telescopetouch.activities.util.ServersReloadListener;
import io.github.marcocipriani01.telescopetouch.activities.views.SameSelectionSpinner;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;
import io.github.marcocipriani01.telescopetouch.util.NSDHelper;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

/**
 * The main screen of the application, which manages the connection.
 *
 * @author Romain Fafet
 * @author marcocipriani01
 */
public class ConnectionFragment extends ActionFragment implements ServersReloadListener,
        ConnectionManager.ManagerListener, NSDHelper.NSDListener, Toolbar.OnMenuItemClickListener {

    private static int selectedSpinnerItem = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private Button connectionButton;
    private SameSelectionSpinner serversSpinner;
    private NSDHelper nsdHelper;
    private EditText portEditText;
    private LogAdapter logAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        setHasOptionsMenu(true);
        logAdapter = new LogAdapter(context);
        RecyclerView logsList = rootView.findViewById(R.id.logs_listview);
        logsList.setAdapter(logAdapter);
        logsList.setLayoutManager(new LinearLayoutManager(context));

        connectionButton = rootView.findViewById(R.id.connectionButton);
        serversSpinner = rootView.findViewById(R.id.spinnerHost);
        nsdHelper = TelescopeTouchApp.getServiceDiscoveryHelper();
        loadServers(ServersActivity.getServers(preferences));
        portEditText = rootView.findViewById(R.id.port_edittext);
        portEditText.setText(String.valueOf(preferences.getInt(ApplicationConstants.INDI_PORT_PREF, 7624)));
        final FragmentActivity activity = getActivity();
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
                    preferences.edit().putInt(ApplicationConstants.INDI_PORT_PREF, port).apply();
                } catch (NumberFormatException e) {
                    port = 7624;
                }
            }
            ConnectionManager.ConnectionState state = connectionManager.getState();
            if (state == ConnectionManager.ConnectionState.DISCONNECTED) {
                if (host.equals(context.getString(R.string.host_add))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    ServersActivity.addServer(context, ConnectionFragment.this);
                } else if (host.equals(context.getString(R.string.host_manage))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    if (activity instanceof MainActivity)
                        ((MainActivity) activity).launchServersActivity();
                } else {
                    connectionManager.connect(host, port);
                }
            } else if (state == ConnectionManager.ConnectionState.CONNECTED) {
                connectionManager.disconnect();
            }
            ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(portEditText.getWindowToken(), 0);
        });
        new ImprovedSpinnerListener() {
            @Override
            protected void onImprovedItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();
                if (selected.equals(context.getString(R.string.host_add))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    ServersActivity.addServer(context, ConnectionFragment.this);
                } else if (selected.equals(context.getString(R.string.host_manage))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    if (activity instanceof MainActivity)
                        ((MainActivity) activity).launchServersActivity();
                }
            }
        }.attach(serversSpinner);

        refreshUi(connectionManager.getState());
        connectionManager.addManagerListener(this);

        if (nsdHelper != null) {
            nsdHelper.setListener(this);
            if (!nsdHelper.isAvailable())
                connectionManager.log(context.getString(R.string.nsd_not_available));
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.connection, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_open_browser) {
            String host = String.valueOf(serversSpinner.getSelectedItem());
            if (host.contains("@")) {
                String[] split = host.split("@");
                if (split.length == 2) host = split[1];
            }
            if (host.equals(context.getString(R.string.host_add)) || host.equals(context.getString(R.string.host_manage))) {
                requestActionSnack(R.string.select_host_first);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + host)));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        selectedSpinnerItem = serversSpinner.getSelectedItemPosition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nsdHelper != null)
            nsdHelper.setListener(null);
        connectionManager.removeManagerListener(this);
    }

    @Override
    public void updateConnectionState(ConnectionManager.ConnectionState state) {
        refreshUi(state);
    }

    @Override
    public void addLog(final ConnectionManager.LogItem log) {
        if (logAdapter != null)
            logAdapter.notifyItemInserted(connectionManager.getLogs().indexOf(log));
        notifyActionChange();
    }

    @Override
    public void deviceLog(final ConnectionManager.LogItem log) {
        if (logAdapter != null) logAdapter.notifyDataSetChanged();
        notifyActionChange();
    }

    private void refreshUi(ConnectionManager.ConnectionState state) {
        switch (state) {
            case CONNECTED: {
                connectionButton.post(() -> {
                    connectionButton.setText(context.getString(R.string.disconnect));
                    connectionButton.setEnabled(true);
                });
                serversSpinner.post(() -> serversSpinner.setEnabled(false));
                portEditText.post(() -> portEditText.setEnabled(false));
                break;
            }
            case DISCONNECTED: {
                connectionButton.post(() -> {
                    connectionButton.setText(context.getString(R.string.connect));
                    connectionButton.setEnabled(true);
                });
                serversSpinner.post(() -> serversSpinner.setEnabled(true));
                portEditText.post(() -> portEditText.setEnabled(true));
                break;
            }
            case BUSY: {
                connectionButton.post(() -> {
                    connectionButton.setText(context.getString(R.string.connecting));
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
        if (nsdHelper != null) {
            HashMap<String, String> services = nsdHelper.getDiscoveredServices();
            for (String name : services.keySet()) {
                String ip = services.get(name);
                if (ip != null) servers.add(name.replace("@", "") + "@" + ip);
            }
        }
        servers.add(context.getString(R.string.host_add));
        servers.add(context.getString(R.string.host_manage));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, servers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serversSpinner.setAdapter(adapter);
        if (adapter.getCount() > selectedSpinnerItem)
            serversSpinner.setSelection(selectedSpinnerItem);
    }

    @Override
    public void onNSDChange() {
        handler.post(() -> loadServers(ServersActivity.getServers(preferences)));
    }

    @Override
    public boolean isActionEnabled() {
        return !connectionManager.getLogs().isEmpty();
    }

    @Override
    public int getActionDrawable() {
        return R.drawable.clear;
    }

    @Override
    public void run() {
        connectionManager.getLogs().clear();
        if (logAdapter != null) logAdapter.notifyDataSetChanged();
        notifyActionChange();
    }

    /**
     * {@code ArrayAdapter} for logs.
     *
     * @author marcocipriani01
     */
    private static class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {

        private final LayoutInflater inflater;

        LogAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LogViewHolder(inflater.inflate(R.layout.logs_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            ConnectionManager.LogItem log = connectionManager.getLogs().get(position);
            holder.log.setText(log.getLog());
            holder.timestamp.setText(log.getTimestamp());
        }

        @Override
        public int getItemCount() {
            return connectionManager.getLogs().size();
        }
    }

    private static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView log, timestamp;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            log = itemView.findViewById(R.id.logs_item1);
            timestamp = itemView.findViewById(R.id.logs_item2);
        }
    }
}