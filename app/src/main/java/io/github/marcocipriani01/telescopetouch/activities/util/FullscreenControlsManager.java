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

package io.github.marcocipriani01.telescopetouch.activities.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Manages the showing and hiding of controls and system UI in full screen mode.
 *
 * @author johntaylor
 * @author marcocipriani01
 */
public class FullscreenControlsManager {

    private static final int AUTO_HIDE_DELAY_MILLIS = 4000;
    private static final int UI_ANIMATION_DELAY = 150;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AppCompatActivity activity;
    private final View[] viewsToHide;
    private final Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            //ActionBar actionBar = activity.getSupportActionBar();
            //if (actionBar != null) actionBar.show();
            for (View view : viewsToHide) {
                view.setVisibility(View.VISIBLE);
            }
        }
    };
    private boolean visible;
    private final Runnable hideRunnable = this::hide;

    @SuppressLint("ClickableViewAccessibility")
    public FullscreenControlsManager(AppCompatActivity activity, View[] viewsToHide, View[] fullscreenTriggers) {
        this.activity = activity;
        visible = true;
        this.viewsToHide = viewsToHide;
        for (View buttonView : fullscreenTriggers) {
            // Touch listener to use for in-layout UI controls to delay hiding the
            // system UI. This is to prevent the jarring behavior of controls going away
            // while interacting with activity UI.
            buttonView.setOnTouchListener((View view, MotionEvent motionEvent) -> {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
                return false;
            });
        }
    }

    public void toggleControls() {
        if (visible) {
            hide();
        } else {
            visible = true;
            handler.postDelayed(showRunnable, UI_ANIMATION_DELAY);
        }
    }

    /**
     * Quickly exposes the controls so that the user knows they're there.
     * Trigger the initial hide() shortly after the activity has been
     * created, to briefly hint to the user that UI controls are available.
     */
    public void flashControls() {
        delayedHide(1000);
    }

    public void delayedHide() {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }

    private void hide() {
        handler.removeCallbacks(showRunnable);
        //ActionBar actionBar = activity.getSupportActionBar();
        //if (actionBar != null) actionBar.hide();
        for (View view : viewsToHide) {
            view.setVisibility(View.GONE);
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        visible = false;
    }

    private void delayedHide(int delayMillis) {
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, delayMillis);
    }
}