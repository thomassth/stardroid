package com.google.android.stardroid.activities

import com.google.android.stardroid.activities.DynamicStarMapActivity
import com.google.android.stardroid.activities.AbstractDynamicStarMapModule
import dagger.Module

/**
 * Dagger module
 * Created by johntaylor on 3/29/16.
 */
@Module
class DynamicStarMapModule(activity: DynamicStarMapActivity?) :
    AbstractDynamicStarMapModule(activity)