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

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
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

import io.github.marcocipriani01.telescopetouch.AppForegroundService;
import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.fragments.AboutFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.ActionFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.AladinFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.BLOBViewerFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.CompassFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.ConnectionFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.ControlPanelFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.FlashlightFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.FocuserFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.GoToFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.MountControlFragment;
import io.github.marcocipriani01.telescopetouch.activities.fragments.PolarisFragment;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;
import io.github.marcocipriani01.telescopetouch.sensors.LocationPermissionRequester;

import static io.github.marcocipriani01.telescopetouch.ApplicationConstants.RECEIVE_BLOB_PREF;
import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

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
    public static final int ACTION_MOUNT_CONTROL = Pages.MOUNT_CONTROL.ordinal();
    public static final int ACTION_SEARCH = Pages.GOTO.ordinal();
    public static final String MESSAGE = "MainActivityMessage";
    private static Pages currentPage = Pages.CONNECTION;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result && (currentPage.lastInstance instanceof LocationPermissionRequester))
                    ((LocationPermissionRequester) currentPage.lastInstance).onLocationPermissionAcquired();
            });
    private SharedPreferences preferences;
    private final ActivityResultLauncher<Intent> serversActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (currentPage.lastInstance instanceof ConnectionFragment)
                    ((ConnectionFragment) currentPage.lastInstance).loadServers(ServersActivity.getServers(preferences));
            });
    private FragmentManager fragmentManager;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;
    private boolean visible = false;
    private boolean doubleBackPressed = false;
    private MenuItem rcvBlobMenuItem;
    private DarkerModeManager darkerModeManager;
    private boolean darkerMode = false;
    private BottomAppBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        darkerModeManager = new DarkerModeManager(this, this, preferences);
        darkerMode = darkerModeManager.getPref();
        setTheme(darkerMode ? R.style.DarkerAppThemeNoActionBar : R.style.AppThemeNoActionBar);
        setContentView(R.layout.activity_main);
        mainCoordinator = findViewById(R.id.main_coordinator);
        fragmentManager = getSupportFragmentManager();
        bottomBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomBar);
        bottomBar.setOnMenuItemClickListener(this);
        final MainBottomNavigation bottomNavigation = new MainBottomNavigation(this);
        bottomBar.setNavigationOnClickListener(v -> bottomNavigation.show());
        fab = findViewById(R.id.main_fab);
        fab.setOnClickListener(v -> {
            bottomBar.performShow();
            if (currentPage.lastInstance instanceof ActionFragment)
                ((ActionFragment) currentPage.lastInstance).run();
        });
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
        handler.postDelayed(() -> {
            int messageRes = intent.getIntExtra(MESSAGE, 0);
            if (messageRes != 0)
                actionSnackRequested(messageRes);
        }, 100);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionManager.addManagerListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AppForegroundService.class.getName().equals(service.service.getClassName())) {
                Intent intent = new Intent(this, AppForegroundService.class);
                intent.setAction(AppForegroundService.ACTION_STOP_SERVICE);
                ContextCompat.startForegroundService(this, intent);
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preferences.getString(ApplicationConstants.EXIT_ACTION_PREF, "0").equals("3") && connectionManager.isConnected()) {
            connectionManager.disconnect();
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionManager.removeManagerListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        String exitAction = preferences.getString(ApplicationConstants.EXIT_ACTION_PREF, "0");
        if (exitAction.equals("1") || (exitAction.equals("2") && connectionManager.isConnected())) {
            Intent intent = new Intent(this, AppForegroundService.class);
            intent.setAction(AppForegroundService.ACTION_START_SERVICE);
            ContextCompat.startForegroundService(this, intent);
        }
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
                handler.postDelayed(() -> doubleBackPressed = false, 2000);
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
                                .setIntent(new Intent(this, SkyMapActivity.class)
                                        .setAction(SkyMapActivity.SKY_MAP_INTENT_ACTION))
                                .setShortLabel(getString(R.string.sky_map))
                                .setIcon(IconCompat.createWithResource(this, R.mipmap.map_launcher))
                                .build(), null);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                }
            } else {
                actionSnackRequested(R.string.shortcuts_not_supported);
            }
        } else if (itemId == R.id.menu_enable_rcv_blob) {
            boolean checked = !item.isChecked();
            item.setChecked(checked);
            connectionManager.setBlobEnabled(checked);
            preferences.edit().putBoolean(RECEIVE_BLOB_PREF, checked).apply();
        } else if (itemId == R.id.menu_darker_mode) {
            if ((currentPage == Pages.ALADIN) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                actionSnackRequested(R.string.dark_mode_not_supported);
            } else {
                darkerModeManager.toggle();
            }
        } else if (itemId == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (itemId == R.id.menu_skymap_diagnostics) {
            startActivity(new Intent(this, DiagnosticActivity.class));
        } else if (currentPage.lastInstance instanceof Toolbar.OnMenuItemClickListener) {
            ((Toolbar.OnMenuItemClickListener) currentPage.lastInstance).onMenuItemClick(item);
        } else {
            return false;
        }
        return true;
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
            startActivity(new Intent(this, SkyMapActivity.class));
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
        Fragment fragment = currentPage.newInstance();
        if (currentPage == Pages.ALADIN) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }
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
        handler.postDelayed(() -> bottomBar.performShow(), 200);
        invalidateOptionsMenu();
    }

    @Override
    public void onActionDrawableChange(int resource) {
        fab.setImageResource(resource);
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
        bottomBar.performShow();
        fab.hide();
        Snackbar.make(mainCoordinator, msgRes, Snackbar.LENGTH_SHORT).setAnchorView(bottomBar)
                .addCallback(new SnackBarCallBack()).show();
    }

    @Override
    public void actionSnackRequested(int msgRes, int actionName, View.OnClickListener action) {
        bottomBar.performShow();
        fab.hide();
        Snackbar.make(mainCoordinator, msgRes, Snackbar.LENGTH_SHORT).setAnchorView(bottomBar)
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

    public void launchServersActivity() {
        serversActivityLauncher.launch(new Intent(this, ServersActivity.class));
    }

    public void requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        CONNECTION(R.id.menu_connection),
        MOUNT_CONTROL(R.id.menu_move),
        GOTO(R.id.menu_goto_fragment),
        CCD_IMAGES(R.id.menu_ccd_images),
        FOCUSER(R.id.menu_focuser),
        CONTROL_PANEL(R.id.menu_generic),
        SKY_MAP(R.id.menu_skymap),
        SKY_MAP_GALLERY(R.id.menu_skymap_gallery),
        ALADIN(R.id.menu_aladin),
        POLARIS(R.id.menu_polaris),
        COMPASS(R.id.menu_compass),
        FLASHLIGHT(R.id.menu_flashlight),
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
                case MOUNT_CONTROL:
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
                case ALADIN:
                    lastInstance = new AladinFragment();
                    break;
                case POLARIS:
                    lastInstance = new PolarisFragment();
                    break;
                case COMPASS:
                    lastInstance = new CompassFragment();
                    break;
                case FLASHLIGHT:
                    lastInstance = new FlashlightFragment();
                    break;
                case ABOUT:
                    lastInstance = new AboutFragment();
                    break;
            }
            return lastInstance;
        }
    }

    private static class MainBottomNavigation extends BottomSheetDialog {

        private final NavigationView.OnNavigationItemSelectedListener listener;
        private Menu navigationMenu;

        MainBottomNavigation(@NonNull MainActivity activity) {
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
            Objects.requireNonNull(navigation).setNavigationItemSelectedListener(item -> {
                dismiss();
                return listener.onNavigationItemSelected(item);
            });
            navigationMenu = navigation.getMenu();
            selectNavItem();
        }

        @Override
        public void show() {
            if (navigationMenu != null)
                selectNavItem();
            super.show();
        }

        private void selectNavItem() {
            for (int i = 0; i < navigationMenu.size(); i++) {
                MenuItem item = navigationMenu.getItem(i);
                item.setChecked(currentPage.itemId == item.getItemId());
            }
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