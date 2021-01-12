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

import android.content.res.Resources;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

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
    /**
     * The connection to the INDI server.
     */
    private INDIServerConnection connection;

    /**
     * Class constructor.
     */
    public ConnectionManager() {
        listeners = new HashSet<>();
    }

    /**
     * @return the current state of this connection manager (connected or not).
     */
    public boolean isConnected() {
        return (connection != null) && (connection.isConnected());
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
        if (!isConnected()) {
            TelescopeTouchApp.setState(TelescopeTouchApp.ConnectionState.CONNECTING);
            TelescopeTouchApp.log(resources.getString(R.string.try_to_connect) + host + ":" + port);
            new Thread(() -> {
                try {
                    connection = new INDIServerConnection(host, port);
                    connection.addINDIServerConnectionListener(this);
                    for (INDIServerConnectionListener l : listeners) {
                        connection.addINDIServerConnectionListener(l);
                    }
                    connection.connect();
                    connection.askForDevices();
                    TelescopeTouchApp.log(resources.getString(R.string.connected));
                    TelescopeTouchApp.setState(TelescopeTouchApp.ConnectionState.CONNECTED);
                } catch (Exception e) {
                    TelescopeTouchApp.log(e.getLocalizedMessage());
                    TelescopeTouchApp.setState(TelescopeTouchApp.ConnectionState.DISCONNECTED);
                }
            }).start();
        } else {
            TelescopeTouchApp.log("Already connected!");
        }
    }

    /**
     * Breaks the connection.
     */
    public void disconnect() {
        if (isConnected()) {
            new Thread(() -> {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    TelescopeTouchApp.log(e.getLocalizedMessage());
                    TelescopeTouchApp.setState(TelescopeTouchApp.ConnectionState.DISCONNECTED);
                }
            }).start();
        } else {
            TelescopeTouchApp.log("Not connected!");
        }
    }

    /**
     * Add a INDIServerConnectionListener to the connection. If the connection
     * is re-created, the listener will be re-installed
     *
     * @param connectionListener the listener
     */
    public void addListener(INDIServerConnectionListener connectionListener) {
        if (connection != null) {
            if (listeners.add(connectionListener)) {
                connection.addINDIServerConnectionListener(connectionListener);
            }
        }
    }

    /**
     * Removes the given listener
     *
     * @param connectionListener the listener
     */
    public void removeListener(INDIServerConnectionListener connectionListener) {
        if (connection != null) {
            listeners.remove(connectionListener);
            connection.removeINDIServerConnectionListener(connectionListener);
        }
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        device.addINDIDeviceListener(this);
        TelescopeTouchApp.log("New device: " + device.getName());
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        device.removeINDIDeviceListener(this);
        TelescopeTouchApp.log("Device removed: " + device.getName());
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        Resources resources = TelescopeTouchApp.getAppResources();
        TelescopeTouchApp.log(resources.getString(R.string.connection_lost));
        TelescopeTouchApp.setState(TelescopeTouchApp.ConnectionState.DISCONNECTED);
        TelescopeTouchApp.goToConnectionTab();
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
        TelescopeTouchApp.log(message);
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {

    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {

    }

    @Override
    public void messageChanged(INDIDevice device) {
        TelescopeTouchApp.log(device.getName() + ": " + device.getLastMessage());
    }
}