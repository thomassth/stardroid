package io.github.marcocipriani01.telescopetouch;

import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import io.github.marcocipriani01.telescopetouch.activities.EditSettingsActivity;
import io.github.marcocipriani01.telescopetouch.activities.ImageDisplayActivity;
import io.github.marcocipriani01.telescopetouch.activities.ImageGalleryActivity;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.control.MagneticDeclinationCalculator;
import io.github.marcocipriani01.telescopetouch.layers.LayerManager;
import io.github.marcocipriani01.telescopetouch.search.SearchTermsProvider;

/**
 * Dagger component.
 * Created by johntaylor on 3/26/16.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    // What we expose to dependent components
    TelescopeTouchApplication provideStardroidApplication();

    SharedPreferences provideSharedPreferences();

    SensorManager provideSensorManager();

    ConnectivityManager provideConnectivityManager();

    AstronomerModel provideAstronomerModel();

    LocationManager provideLocationManager();

    LayerManager provideLayerManager();

    @Named("zero")
    MagneticDeclinationCalculator provideMagDec1();

    @Named("real")
    MagneticDeclinationCalculator provideMagDec2();

    // Who can we inject
    void inject(TelescopeTouchApplication app);

    void inject(EditSettingsActivity activity);

    void inject(ImageDisplayActivity activity);

    void inject(ImageGalleryActivity activity);

    void inject(SearchTermsProvider provider);
}