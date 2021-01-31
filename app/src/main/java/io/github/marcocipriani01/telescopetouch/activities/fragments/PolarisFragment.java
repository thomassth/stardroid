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

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.Polaris;

public class PolarisFragment extends ActionFragment {

    private final Polaris polaris = new Polaris();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private float lastRotation = 0.0f;
    private boolean running = false;
    private boolean wasRunning = true;
    private ImageView skyWatcherReticle;
    private ImageView reticleCrosshair;
    private ImageView bigDipperReticle;
    private TextView spotText;
    private TextView hourAngleText;
    private final Runnable handlerTask = new Runnable() {
        @Override
        public void run() {
            polaris.refresh();
            spotText.setText(polaris.getScopePositionString());
            hourAngleText.setText(polaris.getHourAngleString());
            float rotation = polaris.getScopePosition();
            Animation animation = new RotateAnimation(lastRotation, rotation,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(500);
            animation.setRepeatCount(0);
            animation.setFillAfter(true);
            reticleCrosshair.startAnimation(animation);
            bigDipperReticle.startAnimation(animation);
            lastRotation = rotation;
            handler.postDelayed(handlerTask, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_polaris, container, false);
        skyWatcherReticle = rootView.findViewById(R.id.polaris_reticle_skywatcher);
        reticleCrosshair = rootView.findViewById(R.id.polaris_crosshair);
        bigDipperReticle = rootView.findViewById(R.id.polaris_reticle_big_dipper);
        spotText = rootView.findViewById(R.id.polaris_spot);
        hourAngleText = rootView.findViewById(R.id.polaris_hour_angle);
        polaris.setLocation(41.902782, 12.496366);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setRunning(wasRunning);
        notifyActionDrawableChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        wasRunning = running;
        setRunning(false);
    }

    private void setRunning(boolean running) {
        this.running = running;
        handler.removeCallbacks(handlerTask);
        if (running) {
            handler.postDelayed(handlerTask, 1000);
        }
    }

    @Override
    public void run() {
        running = !running;
        notifyActionDrawableChange();
    }

    @Override
    public boolean isActionEnabled() {
        return true;
    }

    @Override
    public int getActionDrawable() {
        return running ? R.drawable.pause : R.drawable.resume;
    }
}