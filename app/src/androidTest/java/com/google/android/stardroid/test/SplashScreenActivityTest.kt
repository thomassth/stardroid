package com.google.android.stardroid.test

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.SplashScreenActivity
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

/*
If you're running this on your phone and you get an error about
"NoActivityResumed" check you've unlocked your phone.
 */
class SplashScreenActivityTest {
    private class PreferenceCleanerRule : ExternalResource() {
        @Throws(Throwable::class)
        override fun before() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.clear()
            editor.commit()
        }

        override fun after() {
            // code to tear down the external resource
        }
    }

    private val preferenceCleanerRule = PreferenceCleanerRule()
    private val testRule: ActivityScenarioRule<SplashScreenActivity> = ActivityScenarioRule(
        SplashScreenActivity::class.java
    )

    @Rule
    var chain = RuleChain.outerRule(preferenceCleanerRule).around(testRule)
    @Test
    @Throws(InterruptedException::class)
    fun showsWhatsNewAfterTandCs_newUser() {
        Espresso.onView(ViewMatchers.withId(R.id.eula_box_text)).inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(2000)
        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        // TODO: figure out how to dispense with crap like hand-tuned waiting times.
        Thread.sleep(2000)
        // Can't detect this since the UI is still changing.
        // TODO: figure out how we could.
        //onView(withId(R.id.splash)).check(matches(isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.whats_new_box_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    @Throws(InterruptedException::class)
    fun showNoAcceptTandCs() {
        Log.d("TESTTEST", "Doing test")
        Espresso.onView(ViewMatchers.withId(R.id.eula_box_text)).inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        // Decline button
        Espresso.onView(ViewMatchers.withId(android.R.id.button2)).inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        // Sigh. There seems nothing better here.
        Thread.sleep(5000)
        MatcherAssert.assertThat(
            testRule.scenario.state,
            Matchers.equalTo(Lifecycle.State.DESTROYED)
        )
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        MatcherAssert.assertThat(
            appContext.packageName,
            Matchers.`is`("com.google.android.stardroid")
        )
    }
}