package com.google.android.stardroid.activities.util

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import com.google.android.stardroid.activities.util.FullscreenControlsManager
import com.google.android.stardroid.util.MiscUtil.getTag
import java.util.*

/**
 * Manages the showing and hiding of controls and system UI in full screen mode.
 *
 * Created by johntaylor on 2/21/16.
 */
class FullscreenControlsManager(
    private val mActivity: Activity,
    private val contentView: View,
    private val viewsToHide: List<View?>?,
    private val viewsToTriggerHide: List<View>
) {
    private var mContentView: View
    private var mViewsToHide: List<View?>
    private var mVisible = true
    fun toggleControls() {
        Log.d(TAG, "Toggling the UI")
        toggle()
    }

    /**
     * Quickly exposes the controls so that the user knows they're there.
     */
    fun flashTheControls() {

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(INITIALLY_SHOW_CONTROLS_FOR_MILLIS)
    }

    fun delayHideTheControls() {
        delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = OnTouchListener { view, motionEvent ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        for (buttonView in viewsToTriggerHide) {
            buttonView.setOnTouchListener(mDelayHideTouchListener)
        }
        // Hide UI first
        val actionBar = mActivity.actionBar
        actionBar?.hide()
        for (view in mViewsToHide) {
            view!!.visibility = View.GONE
        }
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private val mHidePart2Runnable = Runnable { // Delayed removal of status and navigation bar
        mContentView = contentView

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private val mShowPart2Runnable = Runnable { // Delayed display of UI elements
        mViewsToHide = ArrayList<View?>(viewsToHide)

        val actionBar = mActivity.actionBar
        actionBar?.show()
        for (view in mViewsToHide) {
            view!!.visibility = View.VISIBLE
        }
    }
    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hide() }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        private val TAG = getTag(FullscreenControlsManager::class.java)

        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 1000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
        const val INITIALLY_SHOW_CONTROLS_FOR_MILLIS = 1000
    }

    init {
        mViewsToHide = ArrayList<View?>(viewsToHide)
        mContentView = contentView

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        for (buttonView in viewsToTriggerHide) {
            buttonView.setOnTouchListener(mDelayHideTouchListener)
        }
    }
}