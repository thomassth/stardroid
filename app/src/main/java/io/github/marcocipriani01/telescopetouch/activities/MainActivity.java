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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    /**
     * Last open page.
     */
    private Pages currentPage = Pages.CONNECTION;
    /**
     * The activity's toolbar.
     */
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private BottomNavigationView navigation;
    private boolean visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment()).commit();
        navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.menu_connection);
        navigation.setOnNavigationItemSelectedListener(this);
        TelescopeTouchApp.setGoToConnectionTab(() -> runOnUiThread(() -> {
            if (visible && (navigation != null) && (fragmentManager != null))
                goToConnectionTab();
        }));
    }

    private void goToConnectionTab() {
        currentPage = Pages.CONNECTION;
        toolbar.setElevation(8);
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
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Pages newPage = Pages.fromId(item.getItemId());
        if (newPage == Pages.SKY_MAP) {
            startActivity(new Intent(this, DynamicStarMapActivity.class));
        } else if ((newPage != null) && (newPage != currentPage)) {
            if (newPage == Pages.GENERIC) {
                toolbar.setElevation(0);
            } else {
                toolbar.setElevation(8);
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.values()[newPage.ordinal()].instance).commit();
            currentPage = newPage;
            return true;
        }
        return false;
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        SKY_MAP(R.id.menu_skymap, null),
        CONNECTION(R.id.menu_connection, new ConnectionFragment()),
        MOTION(R.id.menu_move, new MountControlFragment()),
        GENERIC(R.id.menu_generic, new ControlPanelFragment()),
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