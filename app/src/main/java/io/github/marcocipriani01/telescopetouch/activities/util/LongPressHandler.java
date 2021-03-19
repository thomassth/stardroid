/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
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