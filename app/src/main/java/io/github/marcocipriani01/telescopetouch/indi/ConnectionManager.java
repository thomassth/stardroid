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
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Manages an {@link INDIServerConnection} object, listens to INDI messages and notifies listeners.
 *
 * @author marcocipriani01
 */
public class ConnectionManager implements INDIServerConnectionListener, INDIDeviceListener {

    private final Set<ManagerListener> managerListeners = new HashSet<>();
    private final HashSet<INDIServerConnectionListener> indiListeners = new HashSet<>();
    private java.text.DateFormat dateFormat = null;
    private java.text.DateFormat timeFormat = null;
    private INDIServerConnection indiConnection;
    private boolean busy = false;
    private boolean blobEnabled = false;

    public void initFormatters(Context appContext) {
        dateFormat = DateFormat.getDateFormat(appContext);
        timeFormat = DateFormat.getTimeFormat(appContext);
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
                Log.e("ConnectionManager", e.getLocalizedMessage(), e);
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
            Date now = new Date();
            LogItem log = new LogItem(message, dateFormat.format(now) + " " + timeFormat.format(now));
            for (ManagerListener listener : managerListeners) {
                listener.addLog(log);
            }
        }
    }

    /**
     * Add the given message to the logs.
     *
     * @param message a new log.
     */
    public void log(String message, INDIDevice device) {
        if (timeFormat != null) {
            Date now = new Date();
            LogItem log = new LogItem(message, dateFormat.format(now) + " " + timeFormat.format(now), device);
            for (ManagerListener listener : managerListeners) {
                listener.deviceLog(log);
            }
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
        log(TelescopeTouchApp.getAppResources().getString(R.string.new_device) + device.getName());
        new Thread(() -> {
            try {
                device.blobsEnable(this.blobEnabled ? Constants.BLOBEnables.ALSO : Constants.BLOBEnables.NEVER);
            } catch (Exception e) {
                Log.e("ConnectionManager", e.getLocalizedMessage(), e);
            }
        }).start();
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        device.removeINDIDeviceListener(this);
        log(TelescopeTouchApp.getAppResources().getString(R.string.device_remove) + device.getName());
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        busy = false;
        this.indiConnection = null;
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
        new Thread(() -> {
            try {
                if (property instanceof INDIBLOBProperty)
                    device.blobsEnable(this.blobEnabled ? Constants.BLOBEnables.ALSO : Constants.BLOBEnables.NEVER, property);
            } catch (Exception e) {
                Log.e("ConnectionManager", e.getLocalizedMessage(), e);
            }
        }).start();
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {

    }

    @Override
    public void messageChanged(INDIDevice device) {
        log(device.getName() + ": " + device.getLastMessage(), device);
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