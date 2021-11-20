package com.google.android.stardroid.activities.util

import android.app.Activity
import android.app.FragmentManager
import android.content.SharedPreferences
import com.google.android.stardroid.activities.dialogs.LocationPermissionRationaleFragment
import com.google.android.stardroid.control.LocationController
import javax.inject.Inject

/**
 * Created by johntaylor on 4/2/16.
 */
class GooglePlayServicesChecker @Inject internal constructor(
    parent: Activity?, preferences: SharedPreferences?,
    rationaleDialog: LocationPermissionRationaleFragment?,
    fragmentManager: FragmentManager?
) : AbstractGooglePlayServicesChecker(parent, preferences, rationaleDialog, fragmentManager) {
    /**
     * Checks whether play services is available and up to date and prompts the user
     * if necessary.
     *
     *
     * Note that at present we only need it for location services so if the user is setting
     * their location manually we don't do the check.
     */
    override fun maybeCheckForGooglePlayServices() {
//        Log.d(TAG, "Google Play Services check")
        if (preferences!!.getBoolean(LocationController.NO_AUTO_LOCATE, false)) {
//            Log.d(TAG, "Auto location disabled - not checking for GMS")
            return
        }
        super.checkLocationServicesEnabled()
    }
}