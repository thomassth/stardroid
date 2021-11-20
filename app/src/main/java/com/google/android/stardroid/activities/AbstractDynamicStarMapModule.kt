package com.google.android.stardroid.activities

import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.dialogs.*
import com.google.android.stardroid.inject.PerActivity
import com.google.android.stardroid.util.MiscUtil.getTag
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Dagger module
 * Created by johntaylor on 3/29/16.
 */
@Module
open class AbstractDynamicStarMapModule(activity: DynamicStarMapActivity) {
    private val activity: DynamicStarMapActivity
    @Provides
    @PerActivity
    fun provideDynamicStarMapActivity(): DynamicStarMapActivity {
        return activity
    }

    @Provides
    @PerActivity
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    @PerActivity
    fun provideActivityContext(): Context {
        return activity
    }

    @Provides
    @PerActivity
    fun provideEulaDialogFragment(): EulaDialogFragment {
        return EulaDialogFragment()
    }

    @Provides
    @PerActivity
    fun provideTimeTravelDialogFragment(): TimeTravelDialogFragment {
        return TimeTravelDialogFragment()
    }

    @Provides
    @PerActivity
    fun provideHelpDialogFragment(): HelpDialogFragment {
        return HelpDialogFragment()
    }

    @Provides
    @PerActivity
    fun provideNoSearchResultsDialogFragment(): NoSearchResultsDialogFragment {
        return NoSearchResultsDialogFragment()
    }

    @Provides
    @PerActivity
    fun provideMultipleSearchResultsDialogFragment(): MultipleSearchResultsDialogFragment {
        return MultipleSearchResultsDialogFragment()
    }

    @Provides
    @PerActivity
    fun provideNoSensorsDialogFragment(): NoSensorsDialogFragment {
        return NoSensorsDialogFragment()
    }

    @Provides
    @PerActivity
    @Named("timetravel")
    fun provideTimeTravelNoise(): MediaPlayer {
        return MediaPlayer.create(activity, R.raw.timetravel)
    }

    @Provides
    @PerActivity
    @Named("timetravelback")
    fun provideTimeTravelBackNoise(): MediaPlayer {
        return MediaPlayer.create(activity, R.raw.timetravelback)
    }

    @Provides
    @PerActivity
    fun provideTimeTravelFlashAnimation(): Animation {
        return AnimationUtils.loadAnimation(activity, R.anim.timetravelflash)
    }

    @Provides
    @PerActivity
    fun provideHandler(): Handler {
        return Handler()
    }

    @Provides
    @PerActivity
    fun provideFragmentManager(): FragmentManager {
        return activity.fragmentManager
    }

    @Provides
    @PerActivity
    fun provideLocationFragment(): LocationPermissionRationaleFragment {
        return LocationPermissionRationaleFragment()
    }

    companion object {
        private val TAG = getTag(DynamicStarMapModule::class.java)
    }

    init {
        Log.d(TAG, "Creating activity module for $activity")
        this.activity = activity
    }
}