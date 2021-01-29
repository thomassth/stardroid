/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.activities.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Manages the showing and hiding of controls and system UI in full screen mode.
 *
 * @author johntaylor
 * @author marcocipriani01
 */
public class FullscreenControlsManager {

    private static final int AUTO_HIDE_DELAY_MILLIS = 4000;
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AppCompatActivity activity;
    private final View[] viewsToHide;
    private final Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            for (View view : viewsToHide) {
                view.setVisibility(View.VISIBLE);
            }
        }
    };
    private boolean visible;
    private final Runnable hideRunnable = this::hide;

    @SuppressLint("ClickableViewAccessibility")
    public FullscreenControlsManager(AppCompatActivity activity, View[] viewsToHide, View[] viewsToTriggerHide) {
        this.activity = activity;
        visible = true;
        this.viewsToHide = viewsToHide;

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        for (View buttonView : viewsToTriggerHide) {
            /*
             * Touch listener to use for in-layout UI controls to delay hiding the
             * system UI. This is to prevent the jarring behavior of controls going away
             * while interacting with activity UI.
             */
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
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        for (View view : viewsToHide) {
            view.setVisibility(View.GONE);
        }
        visible = false;
    }

    private void delayedHide(int delayMillis) {
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, delayMillis);
    }
}