package com.google.android.stardroid.activities

import android.content.Context
import com.google.android.stardroid.activities.CompassCalibrationActivity
import com.google.android.stardroid.inject.PerActivity
import dagger.Module
import dagger.Provides

/**
 * Created by johntaylor on 4/24/16.
 */
@Module
class CompassCalibrationModule(private val activity: CompassCalibrationActivity) {
    @Provides
    @PerActivity
    fun provideContext(): Context {
        return activity
    }
}