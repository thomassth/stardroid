/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
 *
 * @author johntaylor
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

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

    void inject(TelescopeTouchApp app);

    void inject(SettingsActivity activity);

    void inject(ImageDisplayActivity activity);

    void inject(ImageGalleryActivity activity);

    void inject(SearchTermsProvider provider);
}