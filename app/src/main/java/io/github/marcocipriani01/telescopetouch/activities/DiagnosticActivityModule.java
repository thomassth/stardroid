package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import dagger.Module;
import dagger.Provides;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 4/15/16.
 */
@Module
public class DiagnosticActivityModule {

    private final DiagnosticActivity activity;

    public DiagnosticActivityModule(DiagnosticActivity activity) {
        this.activity = activity;
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
    Handler provideHandler() {
        return new Handler();
    }
}