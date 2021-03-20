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

package io.github.marcocipriani01.telescopetouch.phd2;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.marcocipriani01.simplesocket.SimpleClient;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class PHD2Client extends SimpleClient {

    private static final String TAG = TelescopeTouchApp.getTag(PHD2Client.class);
    private static final int SUPPORTED_MSG_VERSION = 1;
    private static final int RA_COLOR = Color.parseColor("#448AFF");
    private static final int DEC_COLOR = Color.parseColor("#FF1744");
    public final LineGraphSeries<DataPoint> guidingDataRA = new LineGraphSeries<>();
    public final LineGraphSeries<DataPoint> guidingDataDec = new LineGraphSeries<>();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Set<PHD2Listener> listeners = new HashSet<>();
    private final AtomicInteger graphIndex = new AtomicInteger();
    public int raCorrection = 0;
    public boolean raCorrectionSign = false;
    public int decCorrection = 0;
    public boolean decCorrectionSign = false;
    public double hdf = -1.0;
    public double snr = -1.0;
    public volatile String version;
    public volatile AppState appState;
    public volatile ConnectionState connectionState = ConnectionState.UNKNOWN;

    public PHD2Client() {
        super();
        guidingDataRA.setColor(RA_COLOR);
        guidingDataDec.setColor(DEC_COLOR);
    }

    public int getGraphIndex() {
        return graphIndex.get();
    }

    public void addListener(PHD2Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(PHD2Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifyParamUpdate(PHD2Param... params) {
        synchronized (listeners) {
            for (PHD2Listener l : listeners) {
                uiHandler.post(() -> {
                    for (PHD2Param p : params) {
                        l.onPHD2ParamUpdate(p);
                    }
                });
            }
        }
    }

    public void clearGraphReference(GraphView graph) {
        guidingDataRA.clearReference(graph);
        guidingDataDec.clearReference(graph);
    }

    public void clearData() {
        graphIndex.set(0);
        guidingDataRA.resetData(new DataPoint[0]);
        guidingDataDec.resetData(new DataPoint[0]);
    }

    @Override
    protected void onConnected() {
        uiHandler.postDelayed(() -> {
            synchronized (listeners) {
                for (PHD2Listener l : listeners) {
                    l.onPHD2Connected();
                }
            }
        }, 100);
    }

    @Override
    public void disconnect() {
        uiHandler.post(() -> {
            synchronized (listeners) {
                for (PHD2Listener l : listeners) {
                    l.onPHD2Disconnected();
                }
            }
        });
        super.disconnect();
    }

    @Override
    public void onMessage(Socket from, String msg) {
        Log.i(TAG, msg.replace("\n", " | "));
        try {
            JSONObject block = new JSONObject(msg);
            if (block.has("Event")) {
                Event event = Event.get(block.getString("Event"));
                if (event != null) event.parse(this, block);
            }
        } catch (JSONException e) {
            onError(e);
        }
    }

    @Override
    protected void onError(Exception e) {
        Log.e(TAG, e.getMessage(), e);
        uiHandler.post(() -> {
            synchronized (listeners) {
                for (PHD2Listener l : listeners) {
                    l.onPHD2Error(e);
                }
            }
        });
    }

    public void startLoop() {
        println("{\"method\": \"loop\", \"id\": 0}");
    }

    public void findStar() {
        println("{\"method\": \"find_star\", \"id\": 0}");
    }

    public void stopCapture() {
        println("{\"method\": \"stop_capture\", \"id\": 0}");
    }

    public void startGuiding() {
        println("{\"method\": \"guide\", \"params\": {\"settle\": {\"pixels\": 3.0, \"time\": 10, \"timeout\": 40}}, \"id\": 0}");
    }

    //private enum PHD2RPC {
    //}

    public enum PHD2Param {
        VERSION, STATE, GUIDE_VALUES, SETTLE_DONE
    }

    private enum Event {
        Version((phd, msg) -> {
            phd.version = msg.getString("PHDVersion");
            if (msg.getInt("MsgVersion") != SUPPORTED_MSG_VERSION) {
                phd.onError(new UnsupportedOperationException("Unsupported PHD2 version!"));
                phd.disconnect();
            }
        }, PHD2Param.VERSION),
        LockPositionSet(null),
        Calibrating(null),
        CalibrationComplete(null),
        StarSelected(null),
        StartGuiding(null),
        Paused((phd, msg) -> {
            phd.appState = PHD2Client.AppState.Paused;
        }, PHD2Param.STATE),
        StartCalibration((phd, msg) -> {
            phd.appState = PHD2Client.AppState.Calibrating;
        }, PHD2Param.STATE),
        AppState((phd, msg) -> {
            phd.appState = PHD2Client.AppState.fromAppState(msg.getString("State"));
        }, PHD2Param.STATE),
        CalibrationFailed(null),
        CalibrationDataFlipped(null),
        LockPositionShiftLimitReached(null),
        LoopingExposures((phd, msg) -> {
            phd.appState = PHD2Client.AppState.Looping;
        }, PHD2Param.STATE),
        LoopingExposuresStopped((phd, msg) -> {
            phd.appState = PHD2Client.AppState.Stopped;
        }, PHD2Param.STATE),
        SettleBegin(null),
        Settling(null),
        SettleDone(null, PHD2Param.SETTLE_DONE),
        StarLost((phd, msg) -> {
            phd.appState = PHD2Client.AppState.LostLock;
        }, PHD2Param.STATE),
        GuidingStopped(null),
        Resumed(null),
        GuideStep((phd, msg) -> {
            phd.appState = PHD2Client.AppState.Guiding;
            final int maxDataPoints = 200;
            int graphIndex = phd.graphIndex.incrementAndGet();
            DataPoint raVal = new DataPoint(graphIndex, msg.getDouble("RADistanceRaw")),
                    decVal = new DataPoint(graphIndex, msg.getDouble("DECDistanceRaw"));
            phd.uiHandler.post(() -> {
                phd.guidingDataRA.appendData(raVal, true, maxDataPoints);
                phd.guidingDataDec.appendData(decVal, true, maxDataPoints);
            });
            if (msg.has("RADuration")) {
                phd.raCorrection = msg.getInt("RADuration");
                phd.raCorrectionSign = msg.getString("RADirection").equals("East");
            } else {
                phd.raCorrection = 0;
            }
            if (msg.has("DECDuration")) {
                phd.decCorrection = msg.getInt("DECDuration");
                phd.decCorrectionSign = msg.getString("DECDirection").equals("North");
            } else {
                phd.decCorrection = 0;
            }
            phd.hdf = msg.getDouble("HFD");
            phd.snr = msg.getDouble("SNR");
        }, PHD2Param.STATE, PHD2Param.GUIDE_VALUES),
        GuidingDithered(null),
        LockPositionLost(null),
        Alert(null),
        GuideParamChange(null),
        ConfigurationChange(null);

        private final PHD2Action action;
        private final PHD2Param[] updatedParams;

        Event(PHD2Action action, PHD2Param... updatedParams) {
            this.action = action;
            this.updatedParams = updatedParams;
        }

        public static Event get(String event) {
            for (Event s : values()) {
                if (s.name().equals(event)) return s;
            }
            return null;
        }

        void parse(PHD2Client client, JSONObject msg) throws JSONException {
            if (action != null) action.run(client, msg);
            if (updatedParams.length != 0) client.notifyParamUpdate(updatedParams);
        }
    }

    public enum ConnectionState {
        CONNECTED, DISCONNECTED, UNKNOWN
    }

    public enum AppState {
        Stopped(R.string.phd2_stopped),
        Selected(R.string.phd2_selected),
        Calibrating(R.string.phd2_calibrating),
        Guiding(R.string.phd2_guiding),
        LostLock(R.string.phd2_star_lost),
        Paused(R.string.phd2_paused),
        Looping(R.string.phd2_looping);

        private final int descriptionRes;

        AppState(int descriptionRes) {
            this.descriptionRes = descriptionRes;
        }

        public static AppState fromAppState(String appState) {
            for (AppState s : values()) {
                if (s.name().equals(appState)) return s;
            }
            return null;
        }

        public String getDescription(Resources res) {
            return res.getString(descriptionRes);
        }
    }

    public interface PHD2Listener {
        void onPHD2Connected();

        void onPHD2Disconnected();

        void onPHD2Error(Exception e);

        void onPHD2ParamUpdate(PHD2Param param);
    }

    private interface PHD2Action {
        void run(PHD2Client phd, JSONObject msg) throws JSONException;
    }
}