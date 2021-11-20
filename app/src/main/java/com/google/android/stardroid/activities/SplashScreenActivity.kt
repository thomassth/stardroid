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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import com.google.android.stardroid.ApplicationConstants
import com.google.android.stardroid.R
import com.google.android.stardroid.StardroidApplication
import com.google.android.stardroid.activities.DynamicStarMapActivity
import com.google.android.stardroid.activities.SplashScreenActivity
import com.google.android.stardroid.activities.dialogs.EulaDialogFragment
import com.google.android.stardroid.activities.dialogs.EulaDialogFragment.EulaAcceptanceListener
import com.google.android.stardroid.activities.dialogs.WhatsNewDialogFragment
import com.google.android.stardroid.activities.dialogs.WhatsNewDialogFragment.CloseListener
import com.google.android.stardroid.activities.util.ConstraintsChecker
import com.google.android.stardroid.inject.HasComponent
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.MiscUtil.getTag
import javax.inject.Inject

/**
 * Shows a splash screen, then launch the next activity.
 */
class SplashScreenActivity : InjectableActivity(), EulaAcceptanceListener, CloseListener,
    HasComponent<SplashScreenComponent?> {
    @JvmField
    @Inject
    var app: StardroidApplication? = null

    @JvmField
    @Inject
    var analytics: Analytics? = null

    @JvmField
    @Inject
    var sharedPreferences: SharedPreferences? = null

    @JvmField
    @Inject
    var fadeAnimation: Animation? = null

    @JvmField
    @Inject
    var eulaDialogFragmentWithButtons: EulaDialogFragment? = null

    @JvmField
    @Inject
    var whatsNewDialogFragment: WhatsNewDialogFragment? = null

    @JvmField
    @Inject
    var cc: ConstraintsChecker? = null
    private var graphic: View? = null
    override var component: SplashScreenComponent? = null
        private set

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        component = DaggerSplashScreenComponent.builder()
            .applicationComponent(applicationComponent)
            .splashScreenModule(SplashScreenModule(this)).build() as SplashScreenComponent
        component!!.inject(this)
        graphic = findViewById(R.id.splash) as View
        fadeAnimation!!.setAnimationListener(object : AnimationListener {
            override fun onAnimationEnd(unused: Animation) {
                Log.d(TAG, "onAnimationEnd")
                graphic!!.setVisibility(View.INVISIBLE)
                maybeShowWhatsNewAndEnd()
            }

            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationStart(arg0: Animation) {
                Log.d(TAG, "SplashScreen.Animation onAnimationStart")
            }
        })
    }

    public override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        val eulaShowing = maybeShowEula()
        Log.d(TAG, "Eula showing $eulaShowing")
        if (!eulaShowing) {
            // User has previously accepted - let's get on with it!
            Log.d(TAG, "EULA already accepted")
            graphic!!.startAnimation(fadeAnimation)
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    public override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun maybeShowEula(): Boolean {
        val eulaAlreadyConfirmed = sharedPreferences!!.getInt(
            ApplicationConstants.READ_TOS_PREF_VERSION, -1
        ) == EULA_VERSION_CODE
        return if (!eulaAlreadyConfirmed) {
            eulaDialogFragmentWithButtons!!.show(fragmentManager!!, "Eula Dialog")
            true
        } else {
            false
        }
    }

    override fun eulaAccepted() {
        val editor = sharedPreferences!!.edit()
        editor.putInt(ApplicationConstants.READ_TOS_PREF_VERSION, EULA_VERSION_CODE)
        editor.commit()
        // Let's go.
        graphic!!.startAnimation(fadeAnimation)
    }

    override fun eulaRejected() {
        Log.d(TAG, "Sorry chum, no accept, no app.")
        finish()
    }

    private fun maybeShowWhatsNewAndEnd() {
        val whatsNewSeen = sharedPreferences!!.getLong(
            ApplicationConstants.READ_WHATS_NEW_PREF_VERSION, -1
        ) == app!!.version
        if (whatsNewSeen) {
            launchSkyMap()
        } else {
            whatsNewDialogFragment!!.show(fragmentManager!!, "Whats New Dialog")
        }
    }

    // What's new dialog closed.
    override fun dialogClosed() {
        val editor = sharedPreferences!!.edit()
        editor.putLong(ApplicationConstants.READ_WHATS_NEW_PREF_VERSION, app!!.version)
        editor.commit()
        launchSkyMap()
    }

    private fun launchSkyMap() {
        val intent = Intent(this@SplashScreenActivity, DynamicStarMapActivity::class.java)
        cc!!.check()
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = getTag(SplashScreenActivity::class.java)

        // Update this with new versions of the EULA
        private const val EULA_VERSION_CODE = 1
    }
}