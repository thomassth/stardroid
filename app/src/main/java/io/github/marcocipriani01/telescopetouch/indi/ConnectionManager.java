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

package io.github.marcocipriani01.telescopetouch.indi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.properties.INDIStandardElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.layers.TelescopeLayer;

/**
 * Manages an {@link INDIServerConnection} object, listens to INDI messages and notifies listeners.
 *
 * @author marcocipriani01
 */
public class ConnectionManager implements INDIServerConnectionListener, INDIDeviceListener, INDIPropertyListener {

    private static final String TAG = TelescopeTouchApp.getTag(ConnectionManager.class);
    public final EquatorialCoordinates telescopeCoordinates = new EquatorialCoordinates();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<ManagerListener> managerListeners = new HashSet<>();
    private final HashSet<INDIServerConnectionListener> indiListeners = new HashSet<>();
    private final ArrayList<LogItem> logs = new ArrayList<>();
    // Telescope
    public volatile String telescopeName = null;
    public volatile INDINumberProperty telescopeCoordP = null;
    public volatile INDINumberElement telescopeCoordRA = null;
    public volatile INDINumberElement telescopeCoordDec = null;
    public volatile INDISwitchProperty telescopeOnCoordSetP = null;
    public volatile INDISwitchElement telescopeOnCoordSetSync = null;
    public volatile INDISwitchElement telescopeOnCoordSetSlew = null;
    public volatile INDISwitchElement telescopeOnCoordSetTrack = null;
    private java.text.DateFormat dateFormat = null;
    private java.text.DateFormat timeFormat = null;
    private volatile INDIServerConnection indiConnection;
    private volatile boolean busy = false;
    private volatile boolean blobEnabled = false;
    private SharedPreferences preferences;

    public ArrayList<LogItem> getLogs() {
        return logs;
    }

    public void init(Context context) {
        dateFormat = DateFormat.getDateFormat(context);
        timeFormat = DateFormat.getTimeFormat(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(TelescopeLayer.PREFERENCE_ID, false).apply();
    }

    public void setBlobEnabled(boolean b) {
        this.blobEnabled = b;
        new Thread(() -> {
            try {
                if (isConnected()) {
                    Constants.BLOBEnables blobEnables = this.blobEnabled ? Constants.BLOBEnables.ALSO : Constants.BLOBEnables.NEVER;
                    for (INDIDevice device : indiConnection.getDevicesAsList()) {
                        device.blobsEnable(blobEnables);
                        for (INDIProperty<?> property : device.getPropertiesAsList()) {
                            if (property instanceof INDIBLOBProperty)
                                device.blobsEnable(blobEnables, property);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }).start();
    }

    public ConnectionState getState() {
        return busy ? ConnectionState.BUSY :
                (((indiConnection != null) && indiConnection.isConnected()) ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED);
    }

    public boolean isConnected() {
        return (!busy) && (indiConnection != null) && indiConnection.isConnected();
    }

    /**
     * @param state the new state of the Connection button.
     */
    public void updateState(ConnectionState state) {
        for (ManagerListener listener : managerListeners) {
            listener.updateConnectionState(state);
        }
    }

    /**
     * @return the connection. May be {@code null} if the connection doesn't exist.
     */
    public INDIServerConnection getIndiConnection() {
        return indiConnection;
    }

    /**
     * Connects to the driver
     *
     * @param host the host / IP address of the INDI server
     * @param port the port of the INDI server
     */
    public void connect(String host, int port) {
        final Resources resources = TelescopeTouchApp.getAppResources();
        if (getState() == ConnectionState.DISCONNECTED) {
            updateState(ConnectionState.BUSY);
            busy = true;
            log(resources.getString(R.string.try_to_connect) + host + ":" + port);
            new Thread(() -> {
                try {
                    indiConnection = new INDIServerConnection(host, port);
                    indiConnection.addINDIServerConnectionListener(this);
                    for (INDIServerConnectionListener l : indiListeners) {
                        indiConnection.addINDIServerConnectionListener(l);
                    }
                    indiConnection.connect();
                    indiConnection.askForDevices();
                    busy = false;
                    updateState(ConnectionState.CONNECTED);
                    log(resources.getString(R.string.connected));
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                    busy = false;
                    updateState(ConnectionState.DISCONNECTED);
                    log(e.getLocalizedMessage());
                }
            }).start();
        } else {
            log(TelescopeTouchApp.getAppResources().getString(R.string.connection_busy_2));
        }
    }

    /**
     * Add the given message to the logs.
     *
     * @param message a new log.
     */
    public void log(String message) {
        if (timeFormat != null) {
            handler.post(() -> {
                Date now = new Date();
                LogItem log = new LogItem(message, dateFormat.format(now) + " " + timeFormat.format(now));
                logs.add(log);
                for (ManagerListener listener : managerListeners) {
                    listener.addLog(log);
                }
            });
        }
    }

    /**
     * Add the given message to the logs.
     *
     * @param message a new log.
     */
    public void log(String message, INDIDevice device) {
        if (timeFormat != null) {
            handler.post(() -> {
                Date now = new Date();
                LogItem log = new LogItem(message, dateFormat.format(now) + " " + timeFormat.format(now), device);
                for (int i = 0, logsSize = logs.size(); i < logsSize; i++) {
                    if (logs.get(i).getDevice() == log.getDevice()) {
                        logs.remove(i);
                        break;
                    }
                }
                logs.add(log);
                for (ManagerListener listener : managerListeners) {
                    listener.deviceLog(log);
                }
            });
        }
    }

    /**
     * @param listener a new {@link ManagerListener}
     */
    public void addManagerListener(ManagerListener listener) {
        managerListeners.add(listener);
    }

    /**
     * @param listener a new {@link ManagerListener}
     */
    public void removeManagerListener(ManagerListener listener) {
        managerListeners.remove(listener);
    }

    /**
     * Breaks the connection.
     */
    public void disconnect() {
        if (getState() == ConnectionState.CONNECTED) {
            new Thread(() -> {
                busy = true;
                try {
                    indiConnection.disconnect();
                } catch (Exception e) {
                    log(e.getLocalizedMessage());
                }
                indiConnection = null;
                busy = false;
                updateState(ConnectionState.DISCONNECTED);
            }).start();
        } else {
            log(TelescopeTouchApp.getAppResources().getString(R.string.connection_busy));
        }
    }

    /**
     * Add a INDIServerConnectionListener to the connection. If the connection
     * is re-created, the listener will be re-installed
     *
     * @param connectionListener the listener
     */
    public void addINDIListener(INDIServerConnectionListener connectionListener) {
        if (indiListeners.add(connectionListener) && (indiConnection != null))
            indiConnection.addINDIServerConnectionListener(connectionListener);
    }

    /**
     * Removes the given listener
     *
     * @param connectionListener the listener
     */
    public void removeINDIListener(INDIServerConnectionListener connectionListener) {
        indiListeners.remove(connectionListener);
        if (indiConnection != null)
            indiConnection.removeINDIServerConnectionListener(connectionListener);
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        device.addINDIDeviceListener(this);
        log(TelescopeTouchApp.getAppResources().getString(R.string.new_device) + " " + device.getName());
        new Thread(() -> {
            try {
                device.blobsEnable(this.blobEnabled ? Constants.BLOBEnables.ALSO : Constants.BLOBEnables.NEVER);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }).start();
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        device.removeINDIDeviceListener(this);
        log(TelescopeTouchApp.getAppResources().getString(R.string.device_remove) + " " + device.getName());
        if (device.getName().equals(telescopeName))
            clearTelescopeVars();
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        busy = false;
        this.indiConnection = null;
        clearTelescopeVars();
        updateState(ConnectionState.DISCONNECTED);
        log(TelescopeTouchApp.getAppResources().getString(R.string.connection_lost));
        for (ManagerListener listener : managerListeners) {
            listener.onConnectionLost();
        }
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
        log(message);
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        if (property instanceof INDIBLOBProperty) {
            new Thread(() -> {
                try {
                    device.blobsEnable(this.blobEnabled ? Constants.BLOBEnables.ALSO : Constants.BLOBEnables.NEVER, property);
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }).start();
        } else {
            String name = property.getName();
            switch (name) {
                case "EQUATORIAL_EOD_COORD":
                    if (((telescopeCoordDec = (INDINumberElement) property.getElement(INDIStandardElement.DEC)) != null) &&
                            ((telescopeCoordRA = (INDINumberElement) property.getElement(INDIStandardElement.RA)) != null) &&
                            (property instanceof INDINumberProperty)) {
                        property.addINDIPropertyListener(this);
                        telescopeCoordP = (INDINumberProperty) property;
                        telescopeName = device.getName();
                        telescopeCoordinates.ra = telescopeCoordRA.getValue() * 15.0;
                        telescopeCoordinates.dec = telescopeCoordDec.getValue();
                        handler.post(() -> preferences.edit().putBoolean(TelescopeLayer.PREFERENCE_ID, true).apply());
                    }
                    break;
                case "ON_COORD_SET":
                    if (((telescopeOnCoordSetTrack = (INDISwitchElement) property.getElement(INDIStandardElement.TRACK)) != null) &&
                            ((telescopeOnCoordSetSlew = (INDISwitchElement) property.getElement(INDIStandardElement.SLEW)) != null) &&
                            ((telescopeOnCoordSetSync = (INDISwitchElement) property.getElement(INDIStandardElement.SYNC)) != null)) {
                        property.addINDIPropertyListener(this);
                        telescopeOnCoordSetP = (INDISwitchProperty) property;
                    }
                    break;
            }
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        String name = property.getName();
        switch (name) {
            case "EQUATORIAL_EOD_COORD":
                telescopeCoordDec = null;
                telescopeCoordRA = null;
                telescopeCoordP = null;
                telescopeName = null;
                telescopeCoordinates.ra = 0;
                telescopeCoordinates.dec = 0;
                handler.post(() -> preferences.edit().putBoolean(TelescopeLayer.PREFERENCE_ID, false).apply());
                break;
            case "ON_COORD_SET":
                telescopeCoordP = null;
                telescopeCoordRA = null;
                telescopeCoordDec = null;
                break;
        }
    }

    private void clearTelescopeVars() {
        telescopeCoordDec = null;
        telescopeCoordRA = null;
        telescopeCoordP = null;
        telescopeOnCoordSetP = null;
        telescopeOnCoordSetSlew = null;
        telescopeOnCoordSetTrack = null;
        telescopeOnCoordSetSync = null;
        telescopeName = null;
        telescopeCoordinates.ra = 0;
        telescopeCoordinates.dec = 0;
    }

    @Override
    public void messageChanged(INDIDevice device) {
        log(device.getName() + ": " + device.getLastMessage(), device);
    }

    @Override
    public void propertyChanged(INDIProperty<?> indiProperty) {
        if ((indiProperty == telescopeCoordP) && (telescopeCoordRA != null) && (telescopeCoordDec != null)) {
            telescopeCoordinates.ra = telescopeCoordRA.getValue() * 15.0;
            telescopeCoordinates.dec = telescopeCoordDec.getValue();
        }
    }

    public enum ConnectionState {
        DISCONNECTED, CONNECTED, BUSY
    }

    /**
     * This class offers a safe way to update the UI statically instead of keeping in memory Android Widgets,
     * which implement the class {@link Context}.
     *
     * @author marcocipriani01
     */
    public interface ManagerListener {
        default void addLog(LogItem log) {
        }

        default void deviceLog(LogItem log) {
        }

        default void updateConnectionState(ConnectionState state) {
        }

        default void onConnectionLost() {
        }
    }

    /**
     * Represents a single log with its timestamp.
     *
     * @author marcocipriani01
     */
    public static class LogItem {

        private final String log;
        private final String timestamp;
        private INDIDevice device = null;

        LogItem(@NonNull String log, @NonNull String timestamp) {
            this.log = log;
            this.timestamp = timestamp;
        }

        LogItem(@NonNull String log, @NonNull String timestamp, INDIDevice device) {
            this.log = log;
            this.timestamp = timestamp;
            this.device = device;
        }

        public INDIDevice getDevice() {
            return device;
        }

        /**
         * @return the log text.
         */
        public String getLog() {
            return log;
        }

        /**
         * @return the timestamp string.
         */
        public String getTimestamp() {
            return timestamp;
        }
    }
}