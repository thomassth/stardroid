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
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, Toolbar.OnMenuItemClickListener,
        ActionFragment.ActionListener, ConnectionManager.ManagerListener {

    public static final String ACTION = "MainActivityAction";
    public static final int ACTION_CONNECT = Pages.CONNECTION.ordinal();
    public static final int ACTION_SEARCH = Pages.GOTO.ordinal();
    private static Pages currentPage = Pages.CONNECTION;
    private FragmentManager fragmentManager;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;
    private boolean visible = false;
    private boolean doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainCoordinator = findViewById(R.id.main_coordinator);
        fab = findViewById(R.id.main_fab);
        fab.setOnClickListener(v -> {
            Fragment fragment = MainActivity.currentPage.lastInstance;
            if ((fragment instanceof ActionFragment) && fragment.isAdded())
                ((ActionFragment) fragment).run();
        });
        fragmentManager = getSupportFragmentManager();
        BottomAppBar bottomBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomBar);
        bottomBar.setOnMenuItemClickListener(this);
        bottomBar.setNavigationOnClickListener(v -> new MainBottomNavigation(this).show());
        int action = getIntent().getIntExtra(ACTION, -1);
        if (action == -1) {
            showFragment(currentPage, false);
        } else {
            showFragment(Pages.values()[action], false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentPage == Pages.CONNECTION) {
            if (doubleBackPressed) {
                super.onBackPressed();
            } else {
                this.doubleBackPressed = true;
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackPressed = false, 1000);
            }
        } else {
            showFragment(Pages.CONNECTION, true);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_skymap_shortcut) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                ShortcutManagerCompat.requestPinShortcut(this,
                        new ShortcutInfoCompat.Builder(this, "skymap_shortcut")
                                .setIntent(new Intent(this, DynamicStarMapActivity.class)
                                        .setAction(DynamicStarMapActivity.SKY_MAP_INTENT_ACTION))
                                .setShortLabel(getString(R.string.sky_map))
                                .setIcon(IconCompat.createWithResource(this, R.mipmap.map_launcher))
                                .build(), null);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.shortcuts_not_supported), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Pages page = Pages.fromId(item.getItemId());
        if (page == Pages.SKY_MAP_GALLERY) {
            startActivity(new Intent(this, ImageGalleryActivity.class));
            return true;
        } else if (page == Pages.SKY_MAP) {
            startActivity(new Intent(this, DynamicStarMapActivity.class));
            return true;
        } else if ((page != null) && (page != currentPage)) {
            showFragment(page, true);
            return true;
        }
        return false;
    }

    private void showFragment(Pages page, boolean animate) {
        currentPage = page;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (animate)
            transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out);
        Fragment fragment = Objects.requireNonNull(currentPage.getInstance(this));
        transaction.replace(R.id.content_frame, fragment).commit();
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

    @Override
    public void actionSnackRequested(String msg) {
        Snackbar.make(mainCoordinator, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionLost() {
        runOnUiThread(() -> {
            if (currentPage != Pages.CONNECTION) {
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                if (visible && (fragmentManager != null)) showFragment(Pages.CONNECTION, true);
            }
        });
    }

    @Override
    public void addLog(ConnectionManager.LogItem log) {

    }

    @Override
    public void updateConnectionState(ConnectionManager.ConnectionState state) {

    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        CONNECTION(R.id.menu_connection),
        TELESCOPE(R.id.menu_move),
        GOTO(R.id.menu_goto_fragment),
        BLOB_VIEWER(R.id.menu_ccd_images),
        FOCUSER(R.id.menu_focuser),
        CONTROL_PANEL(R.id.menu_generic),
        SKY_MAP(R.id.menu_skymap),
        SKY_MAP_GALLERY(R.id.menu_skymap_gallery),
        COMPASS(R.id.menu_compass),
        ABOUT(R.id.menu_about);

        private final int itemId;
        private Fragment lastInstance;

        Pages(int itemId) {
            this.itemId = itemId;
        }

        private static Pages fromId(int id) {
            for (Pages p : Pages.values()) {
                if (p.itemId == id) return p;
            }
            return null;
        }

        Fragment getInstance(ActionFragment.ActionListener listener) {
            switch (this) {
                case CONNECTION:
                    lastInstance = new ConnectionFragment();
                    break;
                case TELESCOPE:
                    lastInstance = new MountControlFragment();
                    break;
                case GOTO:
                    lastInstance = new GoToFragment();
                    break;
                case BLOB_VIEWER:
                    lastInstance = new BLOBViewerFragment();
                    break;
                case FOCUSER:
                    lastInstance = new FocuserFragment();
                    break;
                case CONTROL_PANEL:
                    lastInstance = new ControlPanelFragment();
                    break;
                case COMPASS:
                    lastInstance = new CompassFragment();
                    break;
                case ABOUT:
                    lastInstance = new AboutFragment();
                    break;
                default:
                    return null;
            }
            if (lastInstance instanceof ActionFragment)
                ((ActionFragment) lastInstance).setActionEnabledListener(listener);
            return lastInstance;
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