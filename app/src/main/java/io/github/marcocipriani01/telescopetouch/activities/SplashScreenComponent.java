package io.github.marcocipriani01.telescopetouch.activities;

import dagger.Component;
import io.github.marcocipriani01.telescopetouch.ApplicationComponent;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.EulaDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.WhatsNewDialogFragment;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 4/2/16.
 */
@PerActivity
@Component(modules = SplashScreenModule.class, dependencies = ApplicationComponent.class)
public interface SplashScreenComponent extends EulaDialogFragment.ActivityComponent,
        WhatsNewDialogFragment.ActivityComponent {
    void inject(SplashScreenActivity activity);
}
