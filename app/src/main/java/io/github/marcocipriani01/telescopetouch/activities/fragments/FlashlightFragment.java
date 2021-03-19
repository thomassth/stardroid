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

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.ScreenLightActivity;

public class FlashlightFragment extends ActionFragment {

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashlight, container, false);
        ToggleButton flashToggle = rootView.findViewById(R.id.flashlight_button);
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            requestActionSnack(R.string.flashlight_unavailable);
            flashToggle.setEnabled(false);
        }
        flashToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    String[] list = cameraManager.getCameraIdList();
                    if (list.length == 0) {
                        requestActionSnack(R.string.flashlight_unavailable);
                        flashToggle.setEnabled(false);
                        return;
                    }
                    cameraManager.setTorchMode(list[0], isChecked);
                } else {
                    Camera camera = Camera.open();
                    Camera.Parameters parameters = camera.getParameters();
                    List<String> modes = parameters.getSupportedFlashModes();
                    if (modes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    } else if (modes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    } else {
                        requestActionSnack(R.string.flashlight_unavailable);
                        flashToggle.setEnabled(false);
                        return;
                    }
                    camera.setParameters(parameters);
                    if (isChecked) {
                        camera.startPreview();
                    } else {
                        camera.stopPreview();
                    }
                }
            } catch (Exception e) {
                requestActionSnack(R.string.flashlight_unavailable);
                flashToggle.setEnabled(false);
            }
        });
        rootView.<Button>findViewById(R.id.screen_flashlight_button)
                .setOnClickListener(v -> startActivity(new Intent(context, ScreenLightActivity.class)));
        return rootView;
    }

    @Override
    public void run() {

    }

    @Override
    public boolean isActionEnabled() {
        return false;
    }

    @Override
    public int getActionDrawable() {
        return 0;
    }
}