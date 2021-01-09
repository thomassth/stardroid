package io.github.marcocipriani01.telescopetouch.activities;

import androidx.appcompat.app.AppCompatActivity;

import io.github.marcocipriani01.telescopetouch.ApplicationComponent;
import io.github.marcocipriani01.telescopetouch.StardroidApplication;

/**
 * Base class for all activities injected by Dagger.
 * <p>
 * Created by johntaylor on 4/9/16.
 */
public abstract class InjectableActivity extends AppCompatActivity {
    protected ApplicationComponent getApplicationComponent() {
        return ((StardroidApplication) getApplication()).getApplicationComponent();
    }
}
