/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch;

import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import io.github.marcocipriani01.telescopetouch.activities.ImageDisplayActivity;
import io.github.marcocipriani01.telescopetouch.activities.ImageGalleryActivity;
import io.github.marcocipriani01.telescopetouch.activities.SettingsActivity;
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
    TelescopeTouchApp provideStardroidApplication();

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
    void inject(TelescopeTouchApp app);

    void inject(SettingsActivity activity);

    void inject(ImageDisplayActivity activity);

    void inject(ImageGalleryActivity activity);

    void inject(SearchTermsProvider provider);
}