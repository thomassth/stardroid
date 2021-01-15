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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener, INDIServerConnectionListener, INDIDeviceListener, INDIPropertyListener {

    /**
     * Last open page.
     */
    private final ArrayList<INDIBLOBProperty> blobProps = new ArrayList<>();
    private final boolean blobEnabled = true;
    private Pages currentPage = Pages.CONNECTION;
    private ConnectionManager connectionManager;
    private AppBarLayout appBarLayout;
    private FragmentManager fragmentManager;
    private BottomNavigationView navigation;
    private boolean visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appBarLayout = findViewById(R.id.appbar_layout);
        setSupportActionBar(findViewById(R.id.app_toolbar));
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment()).commit();
        navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.menu_connection);
        navigation.setOnNavigationItemSelectedListener(this);
        connectionManager = TelescopeTouchApp.getConnectionManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionManager.addListener(this);
        if (connectionManager.isConnected()) {
            INDIServerConnection connection = connectionManager.getConnection();
            List<INDIDevice> list = connection.getDevicesAsList();
            if (list != null) {
                for (INDIDevice device : list) {
                    newDevice(connection, device);
                    for (INDIProperty<?> property : device.getPropertiesAsList()) {
                        newProperty(device, property);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionManager.removeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentPage == Pages.CONNECTION) {
            super.onBackPressed();
        } else {
            goToConnectionTab();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (itemId == R.id.menu_compass) {
            startActivity(new Intent(this, CompassActivity.class));
            return true;
        } else if (itemId == R.id.menu_skymap_shortcut) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(getApplicationContext())) {
                ShortcutManagerCompat.requestPinShortcut(getApplicationContext(),
                        new ShortcutInfoCompat.Builder(getApplicationContext(), "skymap_shortcut")
                                .setIntent(new Intent(getApplicationContext(), DynamicStarMapActivity.class)
                                        .setAction(DynamicStarMapActivity.SKY_MAP_INTENT_ACTION))
                                .setShortLabel(getString(R.string.sky_map))
                                .setIcon(IconCompat.createWithResource(getApplicationContext(), R.mipmap.map_launcher))
                                .build(), null);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.shortcuts_not_supported), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.menu_skymap_gallery) {
            startActivity(new Intent(this, ImageGalleryActivity.class));
            return true;
        } else if (itemId == R.id.menu_goto) {
            startActivity(new Intent(this, GoToActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Pages newPage = Objects.requireNonNull(Pages.fromId(item.getItemId()));
        if (newPage == Pages.SKY_MAP) {
            startActivity(new Intent(this, DynamicStarMapActivity.class));
        } else if (newPage != currentPage) {
            if (newPage == Pages.CONTROL_PANEL) {
                appBarLayout.setElevation(0);
            } else {
                appBarLayout.setElevation(8);
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.values()[newPage.ordinal()].instance).commit();
            currentPage = newPage;
            return true;
        }
        return false;
    }

    private void goToConnectionTab() {
        currentPage = Pages.CONNECTION;
        appBarLayout.setElevation(8);
        navigation.setOnNavigationItemSelectedListener(null);
        navigation.setSelectedItemId(currentPage.itemId);
        navigation.setOnNavigationItemSelectedListener(this);
        try {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.CONNECTION.instance).commit();
        } catch (IllegalStateException e) {
            Log.e("MainActivity", "FragmentManager error", e);
        }
    }

    @Override
    public void newDevice(INDIServerConnection indiServerConnection, INDIDevice device) {
        device.addINDIDeviceListener(this);
        if (blobEnabled) {
            new Thread(() -> {
                try {
                    device.blobsEnable(Constants.BLOBEnables.ALSO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void removeDevice(INDIServerConnection indiServerConnection, INDIDevice device) {
        device.removeINDIDeviceListener(this);
    }

    @Override
    public void connectionLost(INDIServerConnection indiServerConnection) {
        runOnUiThread(() -> {
            if (visible && (navigation != null) && (fragmentManager != null)) goToConnectionTab();
        });
    }

    @Override
    public void newMessage(INDIServerConnection indiServerConnection, Date date, String s) {

    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        if (blobEnabled && (property instanceof INDIBLOBProperty)) {
            property.addINDIPropertyListener(this);
            new Thread(() -> {
                try {
                    device.blobsEnable(Constants.BLOBEnables.ALSO, property);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        property.removeINDIPropertyListener(this);
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        if (blobEnabled && (property instanceof INDIBLOBProperty)) {
            runOnUiThread(() -> Snackbar.make(findViewById(R.id.main_coordinator), "Image received", Snackbar.LENGTH_LONG).setAction("View", (a) -> {
                try {
                    showBlob(property);
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("The received image is unsupported or invalid.");
                    builder.setTitle(property.getDevice().getName());
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                } catch (Error e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Out of memory! The image is too big.");
                    builder.setTitle(property.getDevice().getName());
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
            }).show());
        }
    }

    private void showBlob(INDIProperty<?> property) {
        for (INDIElement element : property.getElementsAsList()) {
            INDIBLOBValue value = ((INDIBLOBElement) element).getValue();
            String format = value.getFormat();
            if (format.equals(".fits")) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(value.getBlobData());
                int read, axis = 0, bitPerPix = 0, width = 0, height = 0;
                byte[] headerBuffer = new byte[80];
                while (inputStream.read(headerBuffer, 0, 80) != -1) {
                    String chunk = new String(headerBuffer);
                    if (chunk.contains("BITPIX")) {
                        bitPerPix = findFITSLineValue(chunk);
                    } else if (chunk.contains("NAXIS1")) {
                        width = findFITSLineValue(chunk);
                    } else if (chunk.contains("NAXIS2")) {
                        height = findFITSLineValue(chunk);
                    } else if (chunk.contains("NAXIS")) {
                        axis = findFITSLineValue(chunk);
                    } else if (chunk.startsWith("END ")) {
                        break;
                    }
                }
                if (axis != 2) throw new UnsupportedOperationException();
                if ((width <= 0) || (height <= 0) || ((bitPerPix != 8) && (bitPerPix != 16)))
                    throw new IllegalStateException();
                short[][] img = new short[width][height];
                int bytesPerPix = bitPerPix / 8;
                byte[] imgBuffer = new byte[bytesPerPix];
                int min = -1, max = -1;
                widthLoop:
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        read = inputStream.read(imgBuffer, 0, bytesPerPix);
                        if (read == -1) break widthLoop;
                        short val;
                        if (bytesPerPix == 2) {
                            if (imgBuffer[1] < 0) {
                                val = (short) ((imgBuffer[0] * 256) + (256 + imgBuffer[1]));
                            } else {
                                val = (short) ((imgBuffer[0] * 256) + imgBuffer[1]);
                            }
                        } else {
                            val = (short) (imgBuffer[0] & 0xFF);
                        }
                        img[w][h] = val;
                        if ((max == -1) || (max < val)) max = val;
                        if ((min == -1) || (min > val)) min = val;
                    }
                }
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                double logMin = Math.log10(min), multiplier = 255.0 / (Math.log10(max) - logMin);
                for (int w = 0; w < width; w++) {
                    for (int h = 0; h < height; h++) {
                        int interpolation = (int) ((Math.log10(img[w][h]) - logMin) * multiplier);
                        bitmap.setPixel(w, h, Color.rgb(interpolation, interpolation, interpolation));
                    }
                }
                ImageView photoView = new PhotoView(this);
                photoView.setImageBitmap(bitmap);
                photoView.setScaleType(ImageView.ScaleType.FIT_XY);
                photoView.setAdjustViewBounds(true);
                new AlertDialog.Builder(this).setView(photoView)
                        .setPositiveButton(android.R.string.ok, (v, e) -> bitmap.recycle()).show();
            } else if (format.equals(".jpg") || format.equals(".jpeg") || format.equals(".png")) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(value.getBlobData());
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                PhotoView photoView = new PhotoView(this);
                photoView.setImageBitmap(bitmap);
                photoView.setScaleType(ImageView.ScaleType.FIT_XY);
                photoView.setAdjustViewBounds(true);
                new AlertDialog.Builder(this).setView(photoView)
                        .setTitle(property.getDevice().getName())
                        .setPositiveButton(android.R.string.ok, (v, e) -> bitmap.recycle()).show();
            }
        }
    }

    private int findFITSLineValue(String in) {
        if (in.contains("=")) in = in.split("=")[1];
        Matcher matcher = Pattern.compile("[0-9]+").matcher(in);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1;
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        SKY_MAP(R.id.menu_skymap, null),
        CONNECTION(R.id.menu_connection, new ConnectionFragment()),
        TELESCOPE(R.id.menu_move, new MountControlFragment()),
        CONTROL_PANEL(R.id.menu_generic, new ControlPanelFragment()),
        FOCUSER(R.id.menu_focuser, new FocuserFragment());

        private final int itemId;
        private final Fragment instance;

        Pages(int itemId, Fragment instance) {
            this.itemId = itemId;
            this.instance = instance;
        }

        private static Pages fromId(int id) {
            for (Pages p : Pages.values()) {
                if (p.itemId == id) return p;
            }
            return null;
        }
    }
}