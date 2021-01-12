package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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
 * Created by johntaylor on 3/29/16.
 */
@Module
public class AbstractDynamicStarMapModule {

    private static final String TAG = TelescopeTouchApp.getTag(DynamicStarMapModule.class);
    private final DynamicStarMapActivity activity;

    public AbstractDynamicStarMapModule(DynamicStarMapActivity activity) {
        Log.d(TAG, "Creating activity module for " + activity);
        this.activity = activity;
    }

    @Provides
    @PerActivity
    DynamicStarMapActivity provideDynamicStarMapActivity() {
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
        return new Handler();
    }

    @Provides
    @PerActivity
    FragmentManager provideFragmentManager() {
        return activity.getSupportFragmentManager();
    }
}