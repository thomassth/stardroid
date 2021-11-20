package com.google.android.stardroid.test

import android.Manifest
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.CompassCalibrationActivity
import com.google.android.stardroid.activities.DynamicStarMapActivity
import com.google.android.stardroid.activities.util.FullscreenControlsManager
import com.google.android.stardroid.control.LocationController
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class DynamicStarMapActivityTest {
    private class SetupRule : ExternalResource() {
        @Throws(Throwable::class)
        override fun before() {
            // We have to set preferences very early otherwise the app starts and doesn't pick them up.
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putBoolean(CompassCalibrationActivity.DONT_SHOW_CALIBRATION_DIALOG, true)
            editor.commit()
        }

        override fun after() {
            // code to tear down the external resource
        }
    }

    private val firstRule = SetupRule()

    @Rule
    var testRule: ActivityScenarioRule<DynamicStarMapActivity> = ActivityScenarioRule(
        DynamicStarMapActivity::class.java
    )

    @Rule
    var chain = RuleChain.outerRule(firstRule).around(testRule)

    // For other great ideas about the permissions dialogs see
    // https://alexzh.com/ui-testing-of-android-runtime-permissions/
    @Rule
    var permissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Before
    fun disableCalibrationDialog() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(CompassCalibrationActivity.DONT_SHOW_CALIBRATION_DIALOG, true)
        editor.putBoolean(
            LocationController.NO_AUTO_LOCATE,
            true
        ) // This disables the Google Play Services check
        editor.commit()
    }

    //@Test
    @Throws(Exception::class)
    fun testSkyMapTouchControlsShowAndThenGo() {
        // Wait for initial controls to go away. This is bad.
        // Perhaps use idling resources?
        Log.w(TAG, "Waiting....")
        Thread.sleep((FullscreenControlsManager.INITIALLY_SHOW_CONTROLS_FOR_MILLIS * 2).toLong())
        Log.w(TAG, "Click")
        Espresso.onView(ViewMatchers.withId(R.id.skyrenderer_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.main_sky_view_root)).perform(ViewActions.click())
        // Espresso should make this kind of crap unnecessary - investigate what's going on...
        // we probably have some ill behaved animation.
        Thread.sleep(100)
        // Not obvious why IsDisplayed not working here?
        Espresso.onView(ViewMatchers.withId(R.id.layer_buttons_control))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Log.w(TAG, "Is visible? Waiting")
        Espresso.onView(ViewMatchers.withId(R.id.main_sky_view_root)).perform(ViewActions.click())
        Thread.sleep(100)
        Espresso.onView(ViewMatchers.withId(R.id.layer_buttons_control)).check(
            ViewAssertions.matches(
                Matchers.not(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )
        )
    }

    companion object {
        private const val TAG = "STARTEST"
    }
}