package com.google.android.stardroid.activities

import android.app.Activity
import android.content.Context
import android.os.Handler
import com.google.android.stardroid.inject.PerActivity
import dagger.Module
import dagger.Provides

/**
 * Created by johntaylor on 4/15/16.
 */
@Module
class DiagnosticActivityModule(private val activity: DiagnosticActivity) {
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
    fun provideHandler(): Handler {
        return Handler()
    }
}