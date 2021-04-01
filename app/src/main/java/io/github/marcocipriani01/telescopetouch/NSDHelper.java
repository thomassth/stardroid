/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class NSDHelper implements ServiceListener {

    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private static final String TAG = TelescopeTouchApp.getTag(NSDHelper.class);
    private final HashMap<String, String> discoveredServices = new HashMap<>();
    private WifiManager.MulticastLock multiCastLock;
    private JmDNS jmdns = null;
    private boolean available = false;
    private NSDListener listener;

    public void start(Context context) {
        if (available) return;
        new Thread(() -> {
            try {
                available = false;
                Log.i(TAG, "Starting Multicast Lock...");
                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                multiCastLock = wifi.createMulticastLock(TAG);
                multiCastLock.setReferenceCounted(true);
                multiCastLock.acquire();
                Log.i(TAG, "Starting ZeroConf probe....");
                int address = wifi.getConnectionInfo().getIpAddress();
                byte[] byteAddress = new byte[]{(byte) (address & 0xff), (byte) (address >> 8 & 0xff),
                        (byte) (address >> 16 & 0xff), (byte) (address >> 24 & 0xff)};
                jmdns = JmDNS.create(InetAddress.getByAddress(byteAddress));
                jmdns.addServiceListener(SERVICE_TYPE, this);
                available = true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                stop();
            }
        }).start();
    }

    public boolean isAvailable() {
        return available;
    }

    public void stop() {
        Log.i(TAG, "Stopping NSD...");
        available = false;
        discoveredServices.clear();
        if (jmdns != null) {
            try {
                jmdns.unregisterAllServices();
                jmdns.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            jmdns = null;
        }
        if (multiCastLock != null) {
            multiCastLock.release();
            multiCastLock = null;
        }
    }

    public void setListener(NSDListener listener) {
        this.listener = listener;
    }

    public HashMap<String, String> getDiscoveredServices() {
        return discoveredServices;
    }

    @Override
    public final void serviceAdded(ServiceEvent event) {
        Log.d(TAG, "Service added: " + event.getInfo());
    }

    @Override
    public final void serviceRemoved(ServiceEvent event) {
        Log.d(TAG, "Service lost: " + event.getInfo());
        discoveredServices.remove(event.getName().replace("@", ""));
        if (listener != null) listener.onNSDChange();
    }

    @Override
    public final void serviceResolved(ServiceEvent event) {
        Log.d(TAG, "Service resolved: " + event.getInfo());
        Inet4Address[] addresses = event.getInfo().getInet4Addresses();
        if (addresses.length > 0) {
            discoveredServices.put(event.getName(), addresses[0].getHostAddress());
            if (listener != null) listener.onNSDChange();
        }
    }

    public interface NSDListener {
        void onNSDChange();
    }
}