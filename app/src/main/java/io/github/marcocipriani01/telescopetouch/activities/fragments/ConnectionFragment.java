/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.annotation.SuppressLint;
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
import io.github.marcocipriani01.telescopetouch.NSDHelper;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.MainActivity;
import io.github.marcocipriani01.telescopetouch.activities.WebManagerActivity;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NewServerDialog;
import io.github.marcocipriani01.telescopetouch.activities.util.ImprovedSpinnerListener;
import io.github.marcocipriani01.telescopetouch.activities.views.SameSelectionSpinner;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;
import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.nsdHelper;
import static io.github.marcocipriani01.telescopetouch.activities.ServersActivity.getServers;

/**
 * The main screen of the application, which manages the connection.
 *
 * @author Romain Fafet
 * @author marcocipriani01
 */
public class ConnectionFragment extends ActionFragment implements ConnectionManager.ManagerListener,
        NSDHelper.NSDListener, Toolbar.OnMenuItemClickListener, NewServerDialog.Callback {

    private static int selectedSpinnerItem = 0;
    private static boolean nsdUnavailableWarned = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private Button connectionButton;
    private SameSelectionSpinner serversSpinner;
    private EditText portEditText;
    private LogAdapter logAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        setHasOptionsMenu(true);
        logAdapter = new LogAdapter(context);
        RecyclerView logsList = rootView.findViewById(R.id.logs_recycler);
        logsList.setAdapter(logAdapter);
        logsList.setLayoutManager(new LinearLayoutManager(context));

        connectionButton = rootView.findViewById(R.id.connect_button);
        serversSpinner = rootView.findViewById(R.id.host_spinner);
        loadServers(getServers(preferences));
        portEditText = rootView.findViewById(R.id.port_field);
        portEditText.setText(String.valueOf(preferences.getInt(ApplicationConstants.INDI_PORT_PREF, 7624)));
        final FragmentActivity activity = getActivity();
        connectionButton.setOnClickListener(v -> {
            ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(portEditText.getWindowToken(), 0);
            Object selectedItem = serversSpinner.getSelectedItem();
            if (selectedItem == null) {
                requestActionSnack(R.string.unknown_error);
                return;
            }
            String host = String.valueOf(selectedItem);
            if (host.equals(context.getString(R.string.host_add))) {
                serversSpinner.post(() -> serversSpinner.setSelection(0));
                NewServerDialog.show(context, preferences, ConnectionFragment.this);
            } else if (host.equals(context.getString(R.string.host_manage))) {
                serversSpinner.post(() -> serversSpinner.setSelection(0));
                if (activity instanceof MainActivity)
                    ((MainActivity) activity).launchServersActivity();
            } else {
                ConnectionManager.State state = connectionManager.getState();
                if (state == ConnectionManager.State.DISCONNECTED) {
                    if (host.contains("@")) {
                        String[] split = host.split("@");
                        if (split.length == 2) host = split[1];
                    }
                    String portStr = portEditText.getText().toString();
                    int port;
                    if (portStr.equals("")) {
                        port = 7624;
                        portEditText.setText("7624");
                    } else {
                        try {
                            port = Integer.parseInt(portStr);
                        } catch (NumberFormatException e) {
                            requestActionSnack(R.string.invalid_port);
                            return;
                        }
                        if ((port < 0) || (port > 0xFFFF)) {
                            requestActionSnack(R.string.invalid_port);
                            return;
                        }
                    }
                    preferences.edit().putInt(ApplicationConstants.INDI_PORT_PREF, port).apply();
                    connectionManager.connect(host, port);
                } else if (state == ConnectionManager.State.CONNECTED) {
                    connectionManager.disconnect();
                }
            }
        });
        new ImprovedSpinnerListener() {
            @Override
            protected void onImprovedItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();
                if (selected.equals(context.getString(R.string.host_add))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    NewServerDialog.show(context, preferences, ConnectionFragment.this);
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
            if ((!nsdHelper.isAvailable()) && (!nsdUnavailableWarned)) {
                connectionManager.log(context.getString(R.string.nsd_not_available));
                nsdUnavailableWarned = true;
            }
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
        int itemId = item.getItemId();
        if (itemId == R.id.menu_open_browser) {
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
        } else if (itemId == R.id.menu_indi_web) {
            String host = String.valueOf(serversSpinner.getSelectedItem());
            if (host.contains("@")) {
                String[] split = host.split("@");
                if (split.length == 2) host = split[1];
            }
            if (host.equals(context.getString(R.string.host_add)) || host.equals(context.getString(R.string.host_manage))) {
                requestActionSnack(R.string.select_host_first);
            } else {
                Intent intent = new Intent(context, WebManagerActivity.class);
                intent.putExtra(WebManagerActivity.INTENT_HOST, host);
                startActivity(intent);
                return true;
            }
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
    public void onDestroy() {
        super.onDestroy();
        if (nsdHelper != null) nsdHelper.setListener(null);
        connectionManager.removeManagerListener(this);
    }

    @Override
    public void updateConnectionState(ConnectionManager.State state) {
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

    private void refreshUi(ConnectionManager.State state) {
        switch (state) {
            case CONNECTED: {
                connectionButton.setText(context.getString(R.string.disconnect));
                connectionButton.setEnabled(true);
                serversSpinner.setEnabled(false);
                portEditText.setEnabled(false);
                break;
            }
            case DISCONNECTED: {
                connectionButton.setText(context.getString(R.string.connect));
                connectionButton.setEnabled(true);
                serversSpinner.setEnabled(true);
                portEditText.setEnabled(true);
                break;
            }
            case BUSY: {
                connectionButton.setText(context.getString(R.string.connecting));
                connectionButton.setEnabled(false);
                serversSpinner.setEnabled(false);
                portEditText.setEnabled(false);
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
        handler.post(() -> loadServers(getServers(preferences)));
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