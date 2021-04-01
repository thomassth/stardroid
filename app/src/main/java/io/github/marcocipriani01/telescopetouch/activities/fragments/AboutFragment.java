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