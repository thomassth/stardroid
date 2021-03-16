package io.github.marcocipriani01.telescopetouch.phd2;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import io.github.marcocipriani01.simplesocket.ConnectionException;
import io.github.marcocipriani01.simplesocket.SimpleClient;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class PHD2Client extends SimpleClient {

    private static final String TAG = TelescopeTouchApp.getTag(PHD2Client.class);
    private static final int SUPPORTED_MSG_VERSION = 1;
    public final LineGraphSeries<DataPoint> guidingDataRA = new LineGraphSeries<>();
    public final LineGraphSeries<DataPoint> guidingDataDec = new LineGraphSeries<>();
    private final Set<PHD2Listener> listeners = new HashSet<>();
    public volatile String version;
    public volatile AppState appState;

    public PHD2Client(String address, int port) {
        super(address, port);
        guidingDataRA.setColor(Color.BLUE);
        guidingDataDec.setColor(Color.RED);
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

    private void notify(PHD2Param... params) {
        synchronized (listeners) {
            for (PHD2Listener l : listeners) {
                for (PHD2Param p : params) {
                    l.onPHD2ParamUpdate(p);
                }
            }
        }
    }

    private void notifyError(Exception e) {
        synchronized (listeners) {
            for (PHD2Listener l : listeners) {
                l.onPHD2CriticalError(e);
            }
        }
    }

    @Override
    protected void close0() throws IOException {
        super.close0();
        synchronized (listeners) {
            for (PHD2Listener l : listeners) {
                l.onPHD2Close();
            }
            listeners.clear();
        }
    }

    @Override
    public void onMessage(Socket from, String msg) {
        Log.i(TAG, msg.replace("\n", " | "));
        try {
            JSONObject block = new JSONObject(msg);
            Event event = Event.get(block.getString("Event"));
            if (event != null) event.parse(this, block);
        } catch (JSONException | ConnectionException e) {
            onError(e);
        }
    }

    @Override
    protected void onError(Exception e) {

    }

    public enum PHD2Param {
        VERSION, STATE, GRAPHS
    }

    public enum Event {
        Version((phd, msg) -> {
            phd.version = msg.getString("PHDVersion");
            if (msg.getInt("MsgVersion") != SUPPORTED_MSG_VERSION) {
                phd.notifyError(new UnsupportedOperationException("Unsupported PHD2 message protocol!"));
                phd.close();
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
        SettleDone(null),
        StarLost((phd, msg) -> {
            phd.appState = PHD2Client.AppState.LostLock;
        }, PHD2Param.STATE),
        GuidingStopped(null),
        Resumed(null),
        GuideStep((phd, msg) -> {
            phd.appState = PHD2Client.AppState.Guiding;
            final int maxDataPoints = 200;
            //TODO: update in main thread
            phd.guidingDataRA.appendData(new DataPoint(msg.getDouble("Time"), msg.getDouble("RADistanceRaw")), true, maxDataPoints);
            phd.guidingDataDec.appendData(new DataPoint(msg.getDouble("Time"), msg.getDouble("DECDistanceRaw")), true, maxDataPoints);
        }, PHD2Param.STATE, PHD2Param.GRAPHS),
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

        void parse(PHD2Client client, JSONObject msg) throws JSONException, ConnectionException {
            if (action != null) {
                action.run(client, msg);
                if (updatedParams.length != 0) client.notify(updatedParams);
            }
        }
    }

    public enum AppState {
        // TODO(marcocipriani01): create string resources
        Stopped(R.string.antares),
        Selected(R.string.antares),
        Calibrating(R.string.antares),
        Guiding(R.string.antares),
        LostLock(R.string.antares),
        Paused(R.string.antares),
        Looping(R.string.antares);

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
        void onPHD2ParamUpdate(PHD2Param param);

        void onPHD2CriticalError(Exception e);

        void onPHD2Close();
    }

    private interface PHD2Action {
        void run(PHD2Client phd, JSONObject msg) throws JSONException, ConnectionException;
    }
}