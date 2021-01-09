package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.FragmentManager;

import dagger.Module;
import dagger.Provides;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.EulaDialogFragment;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 4/2/16.
 */
@Module
public class SplashScreenModule {

    private final SplashScreenActivity activity;

    public SplashScreenModule(SplashScreenActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    EulaDialogFragment provideEulaDialogFragment() {
        EulaDialogFragment fragment = new EulaDialogFragment();
        fragment.setEulaAcceptanceListener(activity);
        return fragment;
    }

    @Provides
    @PerActivity
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    Animation provideFadeoutAnimation() {
        return AnimationUtils.loadAnimation(activity, R.anim.fadeout);
    }

    @Provides
    @PerActivity
    FragmentManager provideFragmentManager() {
        return activity.getSupportFragmentManager();
    }
}