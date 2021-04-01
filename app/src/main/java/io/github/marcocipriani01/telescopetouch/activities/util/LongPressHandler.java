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
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author marcocipriani01
 */
public abstract class LongPressHandler {

    protected final View incrementalView;
    protected final View decrementalView;
    protected final long delay;
    private final Handler handler = new Handler(Looper.getMainLooper());
    protected boolean autoIncrement = false;
    protected boolean autoDecrement = false;
    private final Runnable counterRunnable = new Runnable() {
        @Override
        public void run() {
            if (autoIncrement) {
                onIncrement();
                handler.postDelayed(this, delay);
            } else if (autoDecrement) {
                onDecrement();
                handler.postDelayed(this, delay);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public LongPressHandler(View incrementView, View decrementView, long delay) {
        this.delay = delay;
        this.incrementalView = incrementView;
        this.decrementalView = decrementView;

        this.decrementalView.setOnClickListener(v -> onDecrement());
        this.decrementalView.setOnLongClickListener(v -> {
            autoDecrement = true;
            handler.postDelayed(counterRunnable, LongPressHandler.this.delay);
            return false;
        });
        this.decrementalView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
                autoDecrement = false;
            }
            return false;
        });

        this.incrementalView.setOnClickListener(v -> onIncrement());
        this.incrementalView.setOnLongClickListener(v -> {
            autoIncrement = true;
            handler.postDelayed(counterRunnable, LongPressHandler.this.delay);
            return false;
        });
        this.incrementalView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
                autoIncrement = false;
            }
            return false;
        });
    }

    protected abstract void onIncrement();

    protected abstract void onDecrement();
}