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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.util.Date;
import java.util.Objects;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity implements INDIServerConnectionListener,
        NavigationView.OnNavigationItemSelectedListener, Toolbar.OnMenuItemClickListener, ActionFragment.ActionFragmentListener {

    private static Pages currentPage = Pages.CONNECTION;
    private ConnectionManager connectionManager;
    private FragmentManager fragmentManager;
    private FloatingActionButton fab;
    private boolean visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Pages.setListeners(this);
        BottomAppBar bottomBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomBar);
        bottomBar.setOnMenuItemClickListener(this);
        fragmentManager = getSupportFragmentManager();
        bottomBar.setNavigationOnClickListener(v -> new MainBottomNavigation(this).show());
        fragmentManager.beginTransaction().replace(R.id.content_frame, Pages.CONNECTION.instance).commit();
        fab = findViewById(R.id.main_fab);
        fab.hide();
        fab.setOnClickListener(v -> ((ActionFragment) currentPage.instance).run());
        connectionManager = TelescopeTouchApp.getConnectionManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionManager.addListener(this);
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
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_skymap_shortcut) {
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
        } else if (itemId == R.id.menu_goto) {
            startActivity(new Intent(this, GoToActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Pages newPage = Objects.requireNonNull(Pages.fromId(item.getItemId()));
        if (newPage == Pages.SKY_MAP_GALLERY) {
            startActivity(new Intent(this, ImageGalleryActivity.class));
            return true;
        } else if (newPage == Pages.SKY_MAP) {
            startActivity(new Intent(this, DynamicStarMapActivity.class));
            return true;
        } else if (newPage != currentPage) {
            Fragment fragment = Pages.values()[newPage.ordinal()].instance;
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, fragment).commit();
            if (fragment instanceof ActionFragment) {
                ActionFragment actionFragment = (ActionFragment) fragment;
                fab.setImageResource(actionFragment.getActionDrawable());
                if (actionFragment.isActionEnabled()) {
                    fab.show();
                } else {
                    fab.hide();
                }
            } else {
                fab.hide();
            }
            invalidateOptionsMenu();
            currentPage = newPage;
            return true;
        }
        return false;
    }

    private void goToConnectionTab() {
        currentPage = Pages.CONNECTION;
        try {
            ActionFragment instance = (ActionFragment) Pages.CONNECTION.instance;
            instance.setActionEnabledListener(this);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, instance).commit();
            fab.setImageResource(instance.getActionDrawable());
            setActionEnabled(instance.isActionEnabled());
        } catch (IllegalStateException e) {
            Log.e("MainActivity", "FragmentManager error", e);
        }
    }

    @Override
    public void newDevice(INDIServerConnection indiServerConnection, INDIDevice device) {

    }

    @Override
    public void removeDevice(INDIServerConnection indiServerConnection, INDIDevice device) {

    }

    @Override
    public void connectionLost(INDIServerConnection indiServerConnection) {
        runOnUiThread(() -> {
            if (visible && (fragmentManager != null)) goToConnectionTab();
        });
    }

    @Override
    public void newMessage(INDIServerConnection indiServerConnection, Date date, String s) {

    }

    @Override
    public void setActionEnabled(boolean actionEnabled) {
        if (actionEnabled) {
            fab.show();
        } else {
            fab.hide();
        }
        invalidateOptionsMenu();
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        CONNECTION(R.id.menu_connection, new ConnectionFragment()),
        TELESCOPE(R.id.menu_move, new MountControlFragment()),
        GOTO(R.id.menu_goto_fragment, new GoToFragment()),
        BLOB_VIEWER(R.id.menu_ccd_images, new BLOBViewerFragment()),
        FOCUSER(R.id.menu_focuser, new FocuserFragment()),
        CONTROL_PANEL(R.id.menu_generic, new ControlPanelFragment()),
        SKY_MAP(R.id.menu_skymap, null),
        SKY_MAP_GALLERY(R.id.menu_skymap_gallery, null),
        COMPASS(R.id.menu_compass, new CompassFragment()),
        ABOUT(R.id.menu_about, new AboutFragment());

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

        private static void setListeners(ActionFragment.ActionFragmentListener listener) {
            for (Pages f : values()) {
                if (f.instance instanceof ActionFragment) {
                    ((ActionFragment) f.instance).setActionEnabledListener(listener);
                }
            }
        }
    }

    public static class MainBottomNavigation extends BottomSheetDialog {

        private final NavigationView.OnNavigationItemSelectedListener listener;

        public MainBottomNavigation(@NonNull MainActivity activity) {
            super(activity);
            this.listener = activity;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bottom_drawer);
            getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
            NavigationView navigation = findViewById(R.id.navigation_view);
            if (navigation != null) navigation.setNavigationItemSelectedListener(item -> {
                dismiss();
                return listener.onNavigationItemSelected(item);
            });
        }
    }
}