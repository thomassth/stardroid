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

package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.FragmentManager;

import dagger.Module;
import dagger.Provides;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.MultipleSearchResultsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NoSearchResultsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NoSensorsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.TimeTravelDialogFragment;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Dagger module
 *
 * @author johntaylor
 */
@Module
public class SkyMapModule {

    private static final String TAG = TelescopeTouchApp.getTag(SkyMapModule.class);
    private final SkyMapActivity activity;

    public SkyMapModule(SkyMapActivity activity) {
        Log.d(TAG, "Creating activity module for " + activity);
        this.activity = activity;
    }

    @Provides
    @PerActivity
    SkyMapActivity provideSkyMapActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    Context provideActivityContext() {
        return activity;
    }

    @Provides
    @PerActivity
    TimeTravelDialogFragment provideTimeTravelDialogFragment() {
        return new TimeTravelDialogFragment();
    }

    @Provides
    @PerActivity
    NoSearchResultsDialogFragment provideNoSearchResultsDialogFragment() {
        return new NoSearchResultsDialogFragment();
    }

    @Provides
    @PerActivity
    MultipleSearchResultsDialogFragment provideMultipleSearchResultsDialogFragment() {
        return new MultipleSearchResultsDialogFragment();
    }

    @Provides
    @PerActivity
    NoSensorsDialogFragment provideNoSensorsDialogFragment() {
        return new NoSensorsDialogFragment();
    }

    @Provides
    @PerActivity
    Animation provideTimeTravelFlashAnimation() {
        return AnimationUtils.loadAnimation(activity, R.anim.timetravelflash);
    }

    @Provides
    @PerActivity
    Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @Provides
    @PerActivity
    FragmentManager provideFragmentManager() {
        return activity.getSupportFragmentManager();
    }
}