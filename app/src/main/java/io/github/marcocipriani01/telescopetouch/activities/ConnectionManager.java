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

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * Manages an {@link INDIServerConnection} object, listens to INDI messages and notifies listeners.
 *
 * @author marcocipriani01
 */
public class ConnectionManager implements INDIServerConnectionListener, INDIDeviceListener {

    /**
     * A list to re-add the listener when the connection is destroyed and recreated.
     */
    private final HashSet<INDIServerConnectionListener> listeners;
    private final java.text.DateFormat dateFormat;
    private final java.text.DateFormat timeFormat;
    /**
     * The connection to the INDI server.
     */
    private INDIServerConnection connection;
    /**
     * UI updater
     */
    private UIUpdater uiUpdater = null;
    private boolean busy = false;

    /**
     * Class constructor.
     */
    public ConnectionManager(Context appContext) {
        listeners = new HashSet<>();
        dateFormat = DateFormat.getDateFormat(appContext);
        timeFormat = DateFormat.getTimeFormat(appContext);
    }

    public ConnectionState getState() {
        return busy ? ConnectionState.BUSY :
                (((connection != null) && connection.isConnected()) ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED);
    }

    public boolean isConnected() {
        return (!busy) && (connection != null) && connection.isConnected();
    }

    /**
     * @param state the new state of the Connection button.
     */
    public void updateState(ConnectionState state) {
        if (uiUpdater != null) uiUpdater.updateConnectionState(state);
    }

    /**
     * @return the connection. May be {@code null} if the connection doesn't exist.
     */
    public INDIServerConnection getConnection() {
        return connection;
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
                    connection = new INDIServerConnection(host, port);
                    connection.addINDIServerConnectionListener(this);
                    for (INDIServerConnectionListener l : listeners) {
                        connection.addINDIServerConnectionListener(l);
                    }
                    connection.connect();
                    connection.askForDevices();
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
            log("Already connected or busy!"); //TODO resource
        }
    }

    /**
     * Add the given message to the logs.
     *
     * @param message a new log.
     */
    public void log(String message) {
        if (uiUpdater != null) {
            Date now = new Date();
            LogItem log = new LogItem(message, dateFormat.format(now) + " " + timeFormat.format(now));
            uiUpdater.addLog(log);
        }
    }

    /**
     * @param u a new {@link UIUpdater}
     */
    public void setUiUpdater(UIUpdater u) {
        uiUpdater = u;
    }

    /**
     * Breaks the connection.
     */
    public void disconnect() {
        if (getState() == ConnectionState.CONNECTED) {
            new Thread(() -> {
                busy = true;
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    log(e.getLocalizedMessage());
                }
                connection = null;
                busy = false;
                updateState(ConnectionState.DISCONNECTED);
            }).start();
        } else {
            log("Not connected or busy!"); //TODO resource
        }
    }

    /**
     * Add a INDIServerConnectionListener to the connection. If the connection
     * is re-created, the listener will be re-installed
     *
     * @param connectionListener the listener
     */
    public void addListener(INDIServerConnectionListener connectionListener) {
        if (listeners.add(connectionListener) && (connection != null))
            connection.addINDIServerConnectionListener(connectionListener);
    }

    /**
     * Removes the given listener
     *
     * @param connectionListener the listener
     */
    public void removeListener(INDIServerConnectionListener connectionListener) {
        listeners.remove(connectionListener);
        if (connection != null) connection.removeINDIServerConnectionListener(connectionListener);
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        device.addINDIDeviceListener(this);
        log("New device: " + device.getName()); // TODO resource
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        device.removeINDIDeviceListener(this);
        log("Device removed: " + device.getName()); // TODO resource
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        busy = false;
        this.connection = null;
        updateState(ConnectionState.DISCONNECTED);
        log(TelescopeTouchApp.getAppResources().getString(R.string.connection_lost));
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
        log(message);
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {

    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {

    }

    @Override
    public void messageChanged(INDIDevice device) {
        log(device.getName() + ": " + device.getLastMessage());
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
    public interface UIUpdater {
        /**
         * Appends a log to the Log TextView.
         */
        void addLog(final LogItem log);

        void updateConnectionState(ConnectionState state);
    }

    /**
     * Represents a single log with its timestamp.
     *
     * @author marcocipriani01
     */
    public static class LogItem {

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
}