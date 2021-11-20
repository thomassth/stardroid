// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.activities

import android.app.AlertDialog
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.preference.*
import android.util.Log
import android.widget.Toast
import com.google.android.stardroid.ApplicationConstants
import com.google.android.stardroid.R
import com.google.android.stardroid.StardroidApplication
import com.google.android.stardroid.activities.EditSettingsActivity
import com.google.android.stardroid.activities.util.ActivityLightLevelChanger
import com.google.android.stardroid.activities.util.ActivityLightLevelManager
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.AnalyticsInterface.PREF_KEY
import com.google.android.stardroid.util.MiscUtil.getTag
import java.io.IOException
import javax.inject.Inject

/**
 * Edit the user's preferences.
 */
class EditSettingsActivity : PreferenceActivity() {
    private var preferenceFragment: MyPreferenceFragment? = null

    class MyPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference_screen)
        }
    }

    private var geocoder: Geocoder? = null
    private var activityLightLevelManager: ActivityLightLevelManager? = null

    @JvmField
    @Inject
    var analytics: Analytics? = null

    @JvmField
    @Inject
    var sharedPreferences: SharedPreferences? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as StardroidApplication).applicationComponent.inject(this)
        activityLightLevelManager = ActivityLightLevelManager(
            ActivityLightLevelChanger(this, null),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        geocoder = Geocoder(this)
        preferenceFragment = MyPreferenceFragment()
        fragmentManager.beginTransaction().replace(
            android.R.id.content,
            preferenceFragment
        ).commit()
    }

    public override fun onStart() {
        super.onStart()
        val locationPreference = preferenceFragment!!.findPreference(LOCATION)
        val latitudePreference = preferenceFragment!!.findPreference(LATITUDE)
        val longitudePreference = preferenceFragment!!.findPreference(LONGITUDE)
        locationPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                Log.d(TAG, "Place to be updated to $newValue")
                setLatLongFromPlace(newValue.toString())
            }
        latitudePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                (locationPreference as EditTextPreference).text = ""
                true
            }
        longitudePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                (locationPreference as EditTextPreference).text = ""
                true
            }
        val gyroPreference = preferenceFragment!!.findPreference(
            ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO
        )
        gyroPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                Log.d(TAG, "Toggling gyro preference $newValue")
                enableNonGyroSensorPrefs(newValue as Boolean)
                true
            }
        enableNonGyroSensorPrefs(
            sharedPreferences!!.getBoolean(
                ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO,
                false
            )
        )
    }

    public override fun onResume() {
        super.onResume()
        activityLightLevelManager!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        updatePreferences()
        activityLightLevelManager!!.onPause()
    }

    private fun enableNonGyroSensorPrefs(enabled: Boolean) {
        // These settings aren't compatible with the gyro.
        preferenceFragment!!.findPreference(
            ApplicationConstants.SENSOR_SPEED_PREF_KEY
        ).isEnabled = enabled
        preferenceFragment!!.findPreference(
            ApplicationConstants.SENSOR_DAMPING_PREF_KEY
        ).isEnabled = enabled
        preferenceFragment!!.findPreference(
            ApplicationConstants.REVERSE_MAGNETIC_Z_PREFKEY
        ).isEnabled = enabled
    }

    /**
     * Updates preferences on singletons, so we don't have to register
     * preference change listeners for them.
     */
    private fun updatePreferences() {
        Log.d(TAG, "Updating preferences")
        analytics!!.setEnabled(preferenceFragment!!.findPreference(PREF_KEY).isEnabled)
    }

    protected fun setLatLongFromPlace(place: String): Boolean {
        val addresses: List<Address>
        addresses = try {
            geocoder!!.getFromLocationName(place, 1)
        } catch (e: IOException) {
            Toast.makeText(this, getString(R.string.location_unable_to_geocode), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (addresses.isEmpty()) {
            showNotFoundDialog(place)
            return false
        }
        // TODO(johntaylor) let the user choose, but for now just pick the first.
        val first = addresses[0]
        setLatLong(first.latitude, first.longitude)
        return true
    }

    private fun setLatLong(latitude: Double, longitude: Double) {
        val latPreference = preferenceFragment!!.findPreference(LATITUDE) as EditTextPreference
        val longPreference = preferenceFragment!!.findPreference(LONGITUDE) as EditTextPreference
        latPreference.text = java.lang.Double.toString(latitude)
        longPreference.text = java.lang.Double.toString(longitude)
        val message = String.format(getString(R.string.location_place_found), latitude, longitude)
        Log.d(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showNotFoundDialog(place: String) {
        val message = String.format(getString(R.string.location_not_found), place)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.location_not_found_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
        dialog.show()
    }

    companion object {
        /**
         * These must match the keys in the preference_screen.xml file.
         */
        private const val LONGITUDE = "longitude"
        private const val LATITUDE = "latitude"
        private const val LOCATION = "location"
        private val TAG = getTag(EditSettingsActivity::class.java)
    }
}