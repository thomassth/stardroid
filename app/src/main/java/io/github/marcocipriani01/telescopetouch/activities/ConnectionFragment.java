package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
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

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.views.ImprovedSpinner;
import io.github.marcocipriani01.telescopetouch.views.ImprovedSpinnerListener;

/**
 * The main screen of the application, which manages the connection.
 *
 * @author Romain Fafet
 * @author marcocipriani01
 */
public class ConnectionFragment extends Fragment implements ServersReloadListener, TelescopeTouchApp.UIUpdater {

    /**
     * All the logs.
     */
    private final static ArrayList<LogItem> logs = new ArrayList<>();
    private static final String INDI_PORT_PREF = "INDI_PORT_PREF";
    private static TelescopeTouchApp.ConnectionState state = TelescopeTouchApp.ConnectionState.DISCONNECTED;
    /**
     * The last position of the spinner (to restore the Fragment's state)
     */
    private static int selectedSpinnerItem = 0;
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
    private EditText portEditText;
    /**
     * The original position of the floating action button.
     */
    private int fabPosY;
    private FloatingActionButton clearLogsButton;
    private ListView logsList;
    private LogAdapter logAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) loadServers(ServersActivity.getServers(context));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        logsList = rootView.findViewById(R.id.logsList);
        logAdapter = new LogAdapter(context);
        logsList.setAdapter(logAdapter);
        clearLogsButton = rootView.findViewById(R.id.clearLogsButton);
        clearLogsButton.setOnClickListener(v -> {
            logs.clear();
            logAdapter.notifyDataSetChanged();
            clearLogsButton.animate().translationY(250);
        });
        fabPosY = clearLogsButton.getScrollY();
        if (logs.size() == 0) clearLogsButton.animate().setDuration(0).translationXBy(250);
        logsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem >= visibleItemCount) {
                    clearLogsButton.animate().cancel();
                    clearLogsButton.animate().translationYBy(250);
                } else {
                    clearLogsButton.animate().cancel();
                    clearLogsButton.animate().translationY(fabPosY);
                }
            }
        });

        connectionButton = rootView.findViewById(R.id.connectionButton);
        serversSpinner = rootView.findViewById(R.id.spinnerHost);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        loadServers(ServersActivity.getServers(preferences));
        portEditText = rootView.findViewById(R.id.port_edittext);
        portEditText.setText(String.valueOf(preferences.getInt(INDI_PORT_PREF, 7624)));
        connectionButton.setOnClickListener(v -> {
            // Retrieve Hostname and port number
            String host = String.valueOf(serversSpinner.getSelectedItem());
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
            // Connect or disconnect
            ConnectionManager connectionManager = TelescopeTouchApp.getConnectionManager();
            if (state == TelescopeTouchApp.ConnectionState.DISCONNECTED) {
                if (host.equals(getResources().getString(R.string.host_add))) {
                    serversSpinner.post(() -> serversSpinner.setSelection(0));
                    ServersActivity.addServer(context, ConnectionFragment.this);
                } else if (host.equals(getResources().getString(R.string.host_manage))) {
                    startActivityForResult(new Intent(context, ServersActivity.class), 1);
                } else {
                    connectionManager.connect(host, port);
                }
            } else if (state == TelescopeTouchApp.ConnectionState.CONNECTED) {
                connectionManager.disconnect();
            }
            ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(portEditText.getWindowToken(), 0);
        });
        serversSpinner.setSelection(selectedSpinnerItem);
        spinnerListener.attach(serversSpinner);
        refreshUi();
        TelescopeTouchApp.setUiUpdater(this);
        return rootView;
    }

    @Override
    public void appendLog(final String msg, final String timestamp) {
        logsList.post(() -> {
            logs.add(new LogItem(msg, timestamp));
            logAdapter.notifyDataSetChanged();
            if (logs.size() == 1) {
                clearLogsButton.animate().cancel();
                clearLogsButton.animate().translationY(fabPosY);
            }
        });
    }

    @Override
    public void setConnectionState(TelescopeTouchApp.ConnectionState state) {
        ConnectionFragment.state = state;
        refreshUi();
    }

    private void refreshUi() {
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
            case CONNECTING: {
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
    public void onPause() {
        super.onPause();
        selectedSpinnerItem = serversSpinner.getSelectedItemPosition();
    }

    @Override
    public void loadServers(ArrayList<String> servers) {
        Resources resources = context.getResources();
        servers.add(resources.getString(R.string.host_add));
        servers.add(resources.getString(R.string.host_manage));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, servers);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serversSpinner.setAdapter(dataAdapter);
    }

    /**
     * {@code ViewHolder} for the {@code ListView} that stores logs.
     *
     * @author marcocipriani01
     */
    private static class ViewHolder {
        TextView log, timestamp;
    }

    /**
     * Represents a single log with its timestamp.
     *
     * @author marcocipriani01
     */
    private static class LogItem {

        private final String log;
        private final String timestamp;

        /**
         * Class constructor.
         */
        LogItem(@NonNull String log, @NonNull String timestamp) {
            this.log = log;
            this.timestamp = timestamp;
        }

        /**
         * @return the log text.
         */
        String getLog() {
            return log;
        }

        /**
         * @return the timestamp string.
         */
        String getTimestamp() {
            return timestamp;
        }
    }

    /**
     * {@code ArrayAdapter} for logs.
     *
     * @author marcocipriani01
     */
    private static class LogAdapter extends ArrayAdapter<LogItem> {

        private final LayoutInflater inflater;

        private LogAdapter(Context context) {
            super(context, R.layout.logs_item, logs);
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
            LogItem item = getItem(position);
            if (item != null) {
                holder.log.setText(item.getLog());
                holder.timestamp.setText(item.getTimestamp());
            }
            return convertView;
        }
    }
}