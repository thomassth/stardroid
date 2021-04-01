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
}