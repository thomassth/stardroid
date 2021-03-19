/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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