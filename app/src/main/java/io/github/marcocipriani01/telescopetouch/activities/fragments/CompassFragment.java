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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.CompassCalibrationActivity;
import io.github.marcocipriani01.telescopetouch.activities.MainActivity;
import io.github.marcocipriani01.telescopetouch.sensors.CompassHelper;
import io.github.marcocipriani01.telescopetouch.sensors.LocationPermissionRequester;

import static io.github.marcocipriani01.telescopetouch.maths.Formatters.magDeclinationToString;
import static io.github.marcocipriani01.telescopetouch.maths.Formatters.latitudeToString;
import static io.github.marcocipriani01.telescopetouch.maths.Formatters.longitudeToString;

public class CompassFragment extends ActionFragment implements Toolbar.OnMenuItemClickListener, LocationPermissionRequester {

    private CompassHelper compass;

    @Nullable
    @Override
    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compass, container, false);
        setHasOptionsMenu(true);
        final ImageView arrow = rootView.findViewById(R.id.compass_arrow);
        final TextView heading = rootView.findViewById(R.id.compass_heading);
        final TextView gps = rootView.findViewById(R.id.compass_gps);
        final TextView declination = rootView.findViewById(R.id.compass_declination);
        compass = new CompassHelper(context) {
            private float lastAzimuth = 0;

            @Override
            protected void onLocationAndDeclination(Location location, float magneticDeclination) {
                gps.setText(latitudeToString((float) location.getLatitude(), context) + " / " +
                        longitudeToString((float) location.getLongitude(), context));
                declination.setText(context.getString(R.string.magnetic_declination) + ": " + magDeclinationToString(magneticDeclination, context));
            }

            @Override
            protected void onAzimuth(float azimuth, float arrowRotation) {
                heading.setText(((int) azimuth) + "°");
                Animation animation = new RotateAnimation(lastAzimuth, arrowRotation,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                lastAzimuth = arrowRotation;
                animation.setDuration(500);
                animation.setRepeatCount(0);
                animation.setFillAfter(true);
                arrow.startAnimation(animation);
            }

            @Override
            protected void requestLocationPermission() {
                FragmentActivity activity = getActivity();
                if (activity instanceof MainActivity)
                    ((MainActivity) activity).requestLocationPermission();
            }

            @Override
            protected void onDeclinationEnabledChange(boolean show) {
                int visibility = show ? View.VISIBLE : View.GONE;
                gps.setVisibility(visibility);
                declination.setVisibility(visibility);
            }
        };
        return rootView;
    }

    @Override
    public void onLocationPermissionAcquired() {
        compass.restartLocation();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.compass, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_compass_calibration) {
            Intent intent = new Intent(context, CompassCalibrationActivity.class);
            intent.putExtra(CompassCalibrationActivity.HIDE_CHECKBOX, true);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!compass.start())
            requestActionSnack(R.string.compass_not_available);
    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stop();
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