/*
 * Copyright (C) 2020  Marco Cipriani (@marcocipriani01)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * @author marcocipriani01
 */
public class AboutFragment extends Fragment {

    private NestedScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        scrollView = rootView.findViewById(R.id.about_scrollview);
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        scrollView.stopNestedScroll();
        scrollView.smoothScrollBy(0, 0);
    }
}