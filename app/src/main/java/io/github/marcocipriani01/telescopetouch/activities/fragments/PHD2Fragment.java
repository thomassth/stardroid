/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;

import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.phd2.PHD2Client;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.nsdHelper;
import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.phd2;
import static io.github.marcocipriani01.telescopetouch.activities.ServersActivity.getServers;

public class PHD2Fragment extends ActionFragment implements PHD2Client.PHD2Listener, Slider.OnChangeListener, View.OnClickListener {

    private static int selectedSpinnerItem = 0;
    private SharedPreferences preferences;
    private GraphView graph;
    private Button connectionButton;
    private Spinner serversSpinner;
    private EditText portEditText;
    private TextView statusLabel;
    private TextView raCorrectionLabel, decCorrectionLabel;
    private TextView hdfLabel, snrLabel;
    private ImageButton loopBtn, findStarBtn, guideBtn, stopBtn;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        View rootView = inflater.inflate(R.layout.fragment_phd2, container, false);
        Slider zoomSlider = rootView.findViewById(R.id.phd2_zoom_slider);
        zoomSlider.addOnChangeListener(this);
        statusLabel = rootView.findViewById(R.id.phd2_state);
        raCorrectionLabel = rootView.findViewById(R.id.phd2_correction_ra);
        decCorrectionLabel = rootView.findViewById(R.id.phd2_correction_dec);
        hdfLabel = rootView.findViewById(R.id.phd2_star_mass);
        snrLabel = rootView.findViewById(R.id.phd2_snr);
        loopBtn = rootView.findViewById(R.id.phd_loop_btn);
        loopBtn.setOnClickListener(this);
        findStarBtn = rootView.findViewById(R.id.phd_auto_select_btn);
        findStarBtn.setOnClickListener(this);
        guideBtn = rootView.findViewById(R.id.phd_guide_btn);
        guideBtn.setOnClickListener(this);
        stopBtn = rootView.findViewById(R.id.phd_stop_btn);
        stopBtn.setOnClickListener(this);
        connectionButton = rootView.findViewById(R.id.phd2_connect_button);
        serversSpinner = rootView.findViewById(R.id.phd2_host_spinner);
        loadServers(getServers(preferences));
        portEditText = rootView.findViewById(R.id.phd2_port_field);
        portEditText.setText(String.valueOf(preferences.getInt(ApplicationConstants.PHD2_PORT_PREF, 4400)));

        connectionButton.setOnClickListener(v -> {
            ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(portEditText.getWindowToken(), 0);
            String host = (String) serversSpinner.getSelectedItem();
            if (host == null) {
                requestActionSnack(R.string.unknown_error);
                return;
            }
            try {
                if (phd2.isConnected()) {
                    phd2.disconnect();
                } else {
                    if (host.contains("@")) {
                        String[] split = host.split("@");
                        if (split.length == 2) host = split[1];
                    }
                    String portStr = portEditText.getText().toString();
                    int port;
                    if (portStr.equals("")) {
                        port = 4400;
                        portEditText.setText("4400");
                    } else {
                        try {
                            port = Integer.parseInt(portStr);
                        } catch (NumberFormatException e) {
                            requestActionSnack(R.string.invalid_port);
                            return;
                        }
                        if ((port <= 0) || (port >= 0xFFFF)) {
                            requestActionSnack(R.string.invalid_port);
                            return;
                        }
                    }
                    preferences.edit().putInt(ApplicationConstants.PHD2_PORT_PREF, port).apply();
                    connectionButton.setText(context.getString(R.string.connecting));
                    connectionButton.setEnabled(false);
                    serversSpinner.setEnabled(false);
                    portEditText.setEnabled(false);
                    phd2.connect(host, port);
                }
            } catch (Exception e) {
                errorSnackbar(e);
            }
        });

        NestedScrollView graphTab = rootView.findViewById(R.id.phd2_graph_layout);
        View viewTab = rootView.findViewById(R.id.phd2_live_layout);
        rootView.<TabLayout>findViewById(R.id.phd2_tabs)
                .addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            graphTab.stopNestedScroll();
                            graphTab.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    graphTab.setVisibility(View.GONE);
                                }
                            });
                            viewTab.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    viewTab.setVisibility(View.VISIBLE);
                                    showActionbar();
                                }
                            });
                        } else {
                            graphTab.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    graphTab.setVisibility(View.VISIBLE);
                                }
                            });
                            viewTab.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    viewTab.setVisibility(View.GONE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

        graph = rootView.findViewById(R.id.phd_graph);
        graph.addSeries(phd2.guidingDataRA);
        graph.addSeries(phd2.guidingDataDec);
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setLabelFormatter(new DefaultLabelFormatter());
        gridLabel.setNumHorizontalLabels(4);
        gridLabel.setNumVerticalLabels(6);
        gridLabel.setHorizontalLabelsAngle(45);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-4);
        viewport.setMaxY(4);
        viewport.setScalable(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        phd2.addListener(this);
        if (phd2.isConnected()) {
            onPHD2Connected();
            PHD2Client.AppState state = phd2.appState;
            Resources resources = context.getResources();
            if (state == null) {
                statusLabel.setText(context.getString(R.string.current_state_default));
            } else {
                statusLabel.setText(String.format(resources.getString(R.string.current_state), state.getDescription(resources)));
            }
        } else {
            onPHD2Disconnected();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        selectedSpinnerItem = serversSpinner.getSelectedItemPosition();
    }

    @Override
    public void onStop() {
        super.onStop();
        phd2.removeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (graph != null) phd2.clearGraphReference(graph);
    }

    public void errorSnackbar(Throwable e) {
        requestActionSnack(context.getString(R.string.error) + " " + e.getLocalizedMessage());
    }

    public void loadServers(ArrayList<String> servers) {
        if (nsdHelper.isAvailable()) {
            HashMap<String, String> services = nsdHelper.getDiscoveredServices();
            for (String name : services.keySet()) {
                String ip = services.get(name);
                if (ip != null) servers.add(name.replace("@", "") + "@" + ip);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, servers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serversSpinner.setAdapter(adapter);
        if (adapter.getCount() > selectedSpinnerItem)
            serversSpinner.setSelection(selectedSpinnerItem);
    }

    @Override
    public void onClick(View v) {
        try {
            if (v == loopBtn) {
                phd2.startLoop();
            } else if (v == findStarBtn) {
                phd2.findStar();
            } else if (v == guideBtn) {
                phd2.startGuiding();
            } else if (v == stopBtn) {
                phd2.stopCapture();
            }
        } catch (Exception e) {
            errorSnackbar(e);
        }
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (fromUser && (graph != null)) {
            Viewport viewport = graph.getViewport();
            viewport.setMinY(-value);
            viewport.setMaxY(value);
            graph.invalidate();
            graph.onDataChanged(false, false);
        }
    }

    @Override
    public boolean isActionEnabled() {
        return false;
    }

    @Override
    public int getActionDrawable() {
        return 0;
    }

    @Override
    public void run() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPHD2ParamUpdate(PHD2Client.PHD2Param param) {
        switch (param) {
            case STATE:
                if (statusLabel != null) {
                    Resources resources = context.getResources();
                    statusLabel.setText(String.format(resources.getString(R.string.current_state), phd2.appState.getDescription(resources)));
                }
                break;
            case GUIDE_VALUES:
                int graphIndex = phd2.getGraphIndex();
                if ((graphIndex < 50) && (graph != null)) {
                    Viewport viewport = graph.getViewport();
                    viewport.setMinX(Math.max(0, graphIndex - 50));
                    viewport.setMaxX(graphIndex);
                    graph.onDataChanged(false, false);
                }
                if (raCorrectionLabel != null) {
                    if (phd2.raCorrection == 0) {
                        raCorrectionLabel.setVisibility(View.INVISIBLE);
                    } else {
                        raCorrectionLabel.setText(phd2.raCorrection + " ms");
                        raCorrectionLabel.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,
                                phd2.raCorrectionSign ? R.drawable.arrow_forward : R.drawable.arrow_back), null, null, null);
                        raCorrectionLabel.setVisibility(View.VISIBLE);
                    }
                }
                if (decCorrectionLabel != null) {
                    if (phd2.decCorrection == 0) {
                        decCorrectionLabel.setVisibility(View.INVISIBLE);
                    } else {
                        decCorrectionLabel.setText(phd2.decCorrection + " ms");
                        decCorrectionLabel.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,
                                phd2.decCorrectionSign ? R.drawable.arrow_upward : R.drawable.arrow_downward), null, null, null);
                        decCorrectionLabel.setVisibility(View.VISIBLE);
                    }
                }
                hdfLabel.setText("HDF " + phd2.hdf);
                snrLabel.setText("SNR " + phd2.snr);
                break;
        }
    }

    @Override
    public void onPHD2Connected() {
        connectionButton.setText(context.getString(R.string.disconnect));
        connectionButton.setEnabled(true);
        serversSpinner.setEnabled(false);
        portEditText.setEnabled(false);
        loopBtn.setEnabled(true);
        findStarBtn.setEnabled(true);
        guideBtn.setEnabled(true);
        stopBtn.setEnabled(true);
    }

    @Override
    public void onPHD2Disconnected() {
        connectionButton.setText(context.getString(R.string.connect));
        connectionButton.setEnabled(true);
        serversSpinner.setEnabled(true);
        portEditText.setEnabled(true);
        statusLabel.setText(context.getString(R.string.current_state_default));
        raCorrectionLabel.setVisibility(View.INVISIBLE);
        decCorrectionLabel.setVisibility(View.INVISIBLE);
        hdfLabel.setText(R.string.half_flux_diameter);
        snrLabel.setText(R.string.signal_to_noise);
        loopBtn.setEnabled(false);
        findStarBtn.setEnabled(false);
        guideBtn.setEnabled(false);
        stopBtn.setEnabled(false);
    }

    @Override
    public void onPHD2Error(Exception e) {
        if (!(e instanceof SocketException))
            errorSnackbar(e);
    }
}