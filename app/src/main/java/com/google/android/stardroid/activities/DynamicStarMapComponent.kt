package com.google.android.stardroid.activities

import com.google.android.stardroid.ApplicationComponent
import com.google.android.stardroid.activities.dialogs.*
import com.google.android.stardroid.inject.PerActivity
import dagger.Component

/**
 * Created by johntaylor on 3/29/16.
 */
@PerActivity
@Component(modules = [DynamicStarMapModule::class], dependencies = [ApplicationComponent::class])
interface DynamicStarMapComponent : EulaDialogFragment.ActivityComponent,
    TimeTravelDialogFragment.ActivityComponent, HelpDialogFragment.ActivityComponent,
    NoSearchResultsDialogFragment.ActivityComponent,
    MultipleSearchResultsDialogFragment.ActivityComponent,
    NoSensorsDialogFragment.ActivityComponent {
    fun inject(activity: DynamicStarMapActivity?)
}