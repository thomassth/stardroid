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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

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
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.ActionFragment;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;
import static io.github.marcocipriani01.telescopetouch.activities.BLOBViewerFragment.RECEIVE_BLOB_PREF;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, Toolbar.OnMenuItemClickListener, DarkerModeManager.NightModeListener,
        ActionFragment.ActionListener, ConnectionManager.ManagerListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String ACTION = "MainActivityAction";
    public static final int ACTION_CONNECT = Pages.CONNECTION.ordinal();
    public static final int ACTION_SEARCH = Pages.GOTO.ordinal();
    public static final String MESSAGE = "MainActivityMessage";
    private static Pages currentPage = Pages.CONNECTION;
    private SharedPreferences preferences;
    private FragmentManager fragmentManager;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;
    private boolean visible = false;
    private boolean doubleBackPressed = false;
    private MenuItem rcvBlobMenuItem;
    private DarkerModeManager darkerModeManager;
    private boolean darkerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        darkerModeManager = new DarkerModeManager(getWindow(), this, preferences);
        darkerMode = darkerModeManager.getPref();
        setTheme(darkerMode ? R.style.DarkerAppThemeNoActionBar : R.style.AppThemeNoActionBar);
        setContentView(R.layout.activity_main);
        mainCoordinator = findViewById(R.id.main_coordinator);
        fab = findViewById(R.id.main_fab);
        fab.setOnClickListener(v -> {
            if (currentPage.lastInstance instanceof ActionFragment)
                ((ActionFragment) currentPage.lastInstance).run();
        });
        fragmentManager = getSupportFragmentManager();
        BottomAppBar bottomBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomBar);
        bottomBar.setOnMenuItemClickListener(this);
        MainBottomNavigation bottomNavigation = new MainBottomNavigation(this);
        bottomBar.setNavigationOnClickListener(v -> bottomNavigation.show());
        intentAndFragment(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intentAndFragment(intent);
    }

    private void intentAndFragment(Intent intent) {
        int action = intent.getIntExtra(ACTION, -1);
        if (action == -1) {
            showFragment(currentPage, false);
        } else {
            showFragment(Pages.values()[action], false);
        }
        int messageRes = intent.getIntExtra(MESSAGE, 0);
        if (messageRes != 0)
            actionSnackRequested(messageRes);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionManager.addManagerListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionManager.removeManagerListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
        darkerModeManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
        darkerModeManager.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        rcvBlobMenuItem = menu.findItem(R.id.menu_enable_rcv_blob);
        rcvBlobMenuItem.setChecked(preferences.getBoolean(RECEIVE_BLOB_PREF, false));
        menu.findItem(R.id.menu_darker_mode).setIcon(darkerMode ? R.drawable.light_mode : R.drawable.darker_mode);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (currentPage == Pages.CONNECTION) {
            if (doubleBackPressed) {
                super.onBackPressed();
            } else {
                this.doubleBackPressed = true;
                actionSnackRequested(R.string.press_back_exit);
                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackPressed = false, 2000);
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
                actionSnackRequested(R.string.shortcuts_not_supported);
            }
            return true;
        } else if (itemId == R.id.menu_enable_rcv_blob) {
            boolean checked = !item.isChecked();
            item.setChecked(checked);
            connectionManager.setBlobEnabled(checked);
            preferences.edit().putBoolean(RECEIVE_BLOB_PREF, checked).apply();
            return true;
        } else if (itemId == R.id.menu_darker_mode) {
            darkerModeManager.toggle();
        } else if (currentPage.lastInstance instanceof Toolbar.OnMenuItemClickListener) {
            ((Toolbar.OnMenuItemClickListener) currentPage.lastInstance).onMenuItemClick(item);
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(RECEIVE_BLOB_PREF) && (rcvBlobMenuItem != null))
            rcvBlobMenuItem.setChecked(sharedPreferences.getBoolean(RECEIVE_BLOB_PREF, false));
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
        Fragment fragment = Objects.requireNonNull(currentPage.newInstance());
        transaction.replace(R.id.content_frame, fragment).commit();
        if (fragment instanceof ActionFragment) {
            ActionFragment actionFragment = (ActionFragment) fragment;
            actionFragment.setActionEnabledListener(this);
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
    public void actionSnackRequested(int msgRes) {
        fab.hide();
        Snackbar.make(mainCoordinator, msgRes, Snackbar.LENGTH_SHORT)
                .addCallback(new SnackBarCallBack()).show();
    }

    @Override
    public void actionSnackRequested(int msgRes, int actionName, View.OnClickListener action) {
        fab.hide();
        Snackbar.make(mainCoordinator, msgRes, Snackbar.LENGTH_SHORT)
                .addCallback(new SnackBarCallBack()).setAction(actionName, action).show();
    }

    @Override
    public void onConnectionLost() {
        runOnUiThread(() -> {
            if (currentPage != Pages.CONNECTION) {
                actionSnackRequested(R.string.connection_lost);
                if (visible && (fragmentManager != null)) showFragment(Pages.CONNECTION, true);
            }
        });
    }

    @Override
    public void setNightMode(boolean nightMode) {
        if (nightMode != this.darkerMode) recreate();
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        CONNECTION(R.id.menu_connection),
        TELESCOPE(R.id.menu_move),
        GOTO(R.id.menu_goto_fragment),
        CCD_IMAGES(R.id.menu_ccd_images),
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

        Fragment newInstance() {
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
                case CCD_IMAGES:
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
            getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            NavigationView navigation = findViewById(R.id.navigation_view);
            navigation.setNavigationItemSelectedListener(item -> {
                dismiss();
                return listener.onNavigationItemSelected(item);
            });
            navigation.getMenu().findItem(currentPage.itemId).setChecked(true);
        }
    }

    private class SnackBarCallBack extends BaseTransientBottomBar.BaseCallback<Snackbar> {
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            super.onDismissed(transientBottomBar, event);
            if (currentPage.lastInstance instanceof ActionFragment) {
                ActionFragment actionFragment = (ActionFragment) currentPage.lastInstance;
                actionFragment.setActionEnabledListener(MainActivity.this);
                if (actionFragment.isActionEnabled()) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        }
    }
}