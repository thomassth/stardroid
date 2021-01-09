package io.github.marcocipriani01.telescopetouch.activities;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 4/24/16.
 */
@Module
public class CompassCalibrationModule {
    private final CompassCalibrationActivity activity;

    public CompassCalibrationModule(CompassCalibrationActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    Context provideContext() {
        return activity;
    }
}
