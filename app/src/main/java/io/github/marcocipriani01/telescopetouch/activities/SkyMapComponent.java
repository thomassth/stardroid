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

package io.github.marcocipriani01.telescopetouch.activities;

import dagger.Component;
import io.github.marcocipriani01.telescopetouch.ApplicationComponent;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.MultipleSearchResultsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NoSearchResultsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NoSensorsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.TimeTravelDialogFragment;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 3/29/16.
 */
@PerActivity
@Component(modules = SkyMapModule.class, dependencies = ApplicationComponent.class)
public interface SkyMapComponent extends TimeTravelDialogFragment.ActivityComponent,
        NoSearchResultsDialogFragment.ActivityComponent, MultipleSearchResultsDialogFragment.ActivityComponent,
        NoSensorsDialogFragment.ActivityComponent {
    void inject(SkyMapActivity activity);
}