package io.github.marcocipriani01.telescopetouch.activities;

import dagger.Component;
import io.github.marcocipriani01.telescopetouch.ApplicationComponent;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 4/24/16.
 */
@PerActivity
@Component(modules = CompassCalibrationModule.class, dependencies = ApplicationComponent.class)
public interface CompassCalibrationComponent {
    void inject(CompassCalibrationActivity activity);
}