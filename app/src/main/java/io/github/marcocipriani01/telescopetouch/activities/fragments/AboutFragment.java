/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
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

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * @author marcocipriani01
 */
public class AboutFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

    private NestedScrollView scrollView;
    private MenuItem ossMenuItem;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        setHasOptionsMenu(true);
        scrollView = rootView.findViewById(R.id.about_scrollview);
        ImageView icon = rootView.findViewById(R.id.icon_view);
        icon.setOnTouchListener((v, motionEvent) -> {
            ObjectAnimator animY = ObjectAnimator.ofFloat(icon, "translationY", 30f, 0f);
            animY.setDuration(200);
            animY.setInterpolator(new BounceInterpolator());
            animY.start();
            return false;
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        ossMenuItem = menu.add(R.string.open_source_licenses);
        ossMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStop() {
        super.onStop();
        scrollView.stopNestedScroll();
        scrollView.smoothScrollBy(0, 0);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item == ossMenuItem)
            startActivity(new Intent(getContext(), OssLicensesMenuActivity.class));
        return false;
    }
}