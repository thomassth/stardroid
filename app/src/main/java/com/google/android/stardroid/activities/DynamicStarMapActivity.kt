// Copyright 2010 Google Inc.
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

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.stardroid.ApplicationConstants
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.dialogs.*
import com.google.android.stardroid.activities.util.ActivityLightLevelChanger
import com.google.android.stardroid.activities.util.ActivityLightLevelManager
import com.google.android.stardroid.activities.util.FullscreenControlsManager
import com.google.android.stardroid.activities.util.GooglePlayServicesChecker
import com.google.android.stardroid.base.Lists.asList
import com.google.android.stardroid.control.AstronomerModel
import com.google.android.stardroid.control.ControllerGroup
import com.google.android.stardroid.control.MagneticDeclinationCalculatorSwitcher
import com.google.android.stardroid.inject.HasComponent
import com.google.android.stardroid.layers.LayerManager
import com.google.android.stardroid.math.MathUtils.atan2
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.math.getGeocentricCoords
import com.google.android.stardroid.renderer.RendererController
import com.google.android.stardroid.renderer.SkyRenderer
import com.google.android.stardroid.renderer.util.AbstractUpdateClosure
import com.google.android.stardroid.search.SearchResult
import com.google.android.stardroid.touch.DragRotateZoomGestureDetector
import com.google.android.stardroid.touch.GestureInterpreter
import com.google.android.stardroid.touch.MapMover
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.AnalyticsInterface.*
import com.google.android.stardroid.util.MiscUtil.getTag
import com.google.android.stardroid.util.SensorAccuracyMonitor
import com.google.android.stardroid.views.ButtonLayerView
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * The main map-rendering Activity.
 */
class DynamicStarMapActivity : InjectableActivity(), OnSharedPreferenceChangeListener,
    HasComponent<DynamicStarMapComponent?> {
    private var fullscreenControlsManager: FullscreenControlsManager? = null

    /**
     * Passed to the renderer to get per-frame updates from the model.
     *
     * @author John Taylor
     */
    private class RendererModelUpdateClosure(
        private val model: AstronomerModel?,
        private val rendererController: RendererController, sharedPreferences: SharedPreferences?
    ) : AbstractUpdateClosure() {
        private val horizontalRotation: Boolean
        override fun run() {
            val pointing = model!!.pointing
            val directionX = pointing.lineOfSightX
            val directionY = pointing.lineOfSightY
            val directionZ = pointing.lineOfSightZ
            val upX = pointing.perpendicularX
            val upY = pointing.perpendicularY
            val upZ = pointing.perpendicularZ
            rendererController.queueSetViewOrientation(
                directionX,
                directionY,
                directionZ,
                upX,
                upY,
                upZ
            )
            val (x, y) = model.phoneUpDirection
            rendererController.queueTextAngle(atan2(x, y))
            rendererController.queueViewerUpDirection(model.zenith.copyForJ())
            val fieldOfView = model.fieldOfView
            rendererController.queueFieldOfView(fieldOfView)
        }

        init {
            horizontalRotation =
                sharedPreferences!!.getBoolean(ApplicationConstants.ROTATE_HORIZON_PREFKEY, false)
            model!!.setHorizontalRotation(horizontalRotation)
        }
    }

    private var cancelSearchButton: ImageButton? = null

    @JvmField
    @Inject
    var controller: ControllerGroup? = null
    private var gestureDetector: GestureDetector? = null

    @JvmField
    @Inject
    var model: AstronomerModel? = null
    private var rendererController: RendererController? = null
    private var nightMode = false
    private var searchMode = false
    private var searchTarget: Vector3? = getGeocentricCoords(0f, 0f)

    @JvmField
    @Inject
    var sharedPreferences: SharedPreferences? = null
    private var skyView: GLSurfaceView? = null
    private var wakeLock: WakeLock? = null
    private var searchTargetName: String? = null

    @JvmField
    @Inject
    var layerManager: LayerManager? = null

    // TODO(widdows): Figure out if we should break out the
    // time dialog and time player into separate activities.
    private var timePlayerUI: View? = null
    override var component: DynamicStarMapComponent? = null
        private set

    @JvmField
    @Inject
    @Named("timetravel")
    var timeTravelNoiseProvider: Provider<MediaPlayer>? = null

    @JvmField
    @Inject
    @Named("timetravelback")
    var timeTravelBackNoiseProvider: Provider<MediaPlayer>? = null
    private var timeTravelNoise: MediaPlayer? = null
    private var timeTravelBackNoise: MediaPlayer? = null

    @JvmField
    @Inject
    var handler: Handler? = null

    @JvmField
    @Inject
    var analytics: Analytics? = null

    @JvmField
    @Inject
    var playServicesChecker: GooglePlayServicesChecker? = null


    @JvmField
    @Inject
    var eulaDialogFragmentNoButtons: EulaDialogFragment? = null

    @JvmField
    @Inject
    var timeTravelDialogFragment: TimeTravelDialogFragment? = null

    @JvmField
    @Inject
    var helpDialogFragment: HelpDialogFragment? = null

    @JvmField
    @Inject
    var noSearchResultsDialogFragment: NoSearchResultsDialogFragment? = null

    @JvmField
    @Inject
    var multipleSearchResultsDialogFragment: MultipleSearchResultsDialogFragment? = null

    @JvmField
    @Inject
    var noSensorsDialogFragment: NoSensorsDialogFragment? = null

    @JvmField
    @Inject
    var sensorAccuracyMonitor: SensorAccuracyMonitor? = null

    // A list of runnables to post on the handler when we resume.
    private val onResumeRunnables: MutableList<Runnable> = ArrayList()

    // We need to maintain references to these objects to keep them from
    // getting gc'd.
    @JvmField
    @Inject
    var magneticSwitcher: MagneticDeclinationCalculatorSwitcher? = null
    private var dragZoomRotateDetector: DragRotateZoomGestureDetector? = null

    @JvmField
    @Inject
    var flashAnimation: Animation? = null
    private var activityLightLevelManager: ActivityLightLevelManager? = null
    private var sessionStartTime: Long = 0
    public override fun onCreate(icicle: Bundle?) {
        Log.d(TAG, "onCreate at " + System.currentTimeMillis())
        super.onCreate(icicle)
        component = DaggerDynamicStarMapComponent.builder()
            .applicationComponent(applicationComponent)
            .dynamicStarMapModule(DynamicStarMapModule(this)).build() as DynamicStarMapComponent
        component!!.inject(this)
        sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)

        // Set up full screen mode, hide the system UI etc.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TODO(jontayler): upgrade to
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        // when we reach API level 16.
        // http://developer.android.com/training/system-ui/immersive.html for the right way
        // to do it at API level 19.
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // Eventually we should check at the point of use, but this will do for now.  If the
        // user revokes the permission later then odd things may happen.
        playServicesChecker!!.maybeCheckForGooglePlayServices()
        initializeModelViewController()
        checkForSensorsAndMaybeWarn()

        // Search related
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)
        val activityLightLevelChanger = ActivityLightLevelChanger(
            this
        ) { nightMode1 -> rendererController!!.queueNightVisionMode(nightMode1) }
        activityLightLevelManager = ActivityLightLevelManager(
            activityLightLevelChanger,
            sharedPreferences as SharedPreferences
        )
        val pm = ContextCompat.getSystemService(this, PowerManager::class.java)
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG)
        }

        // Were we started as the result of a search?
        val intent = intent
        Log.d(TAG, "Intent received: $intent")
        if (Intent.ACTION_SEARCH == intent.action) {
            Log.d(TAG, "Started as a result of a search")
            doSearchWithIntent(intent)
        }
        Log.d(TAG, "-onCreate at " + System.currentTimeMillis())
    }

    private fun checkForSensorsAndMaybeWarn() {
        val sensorManager = ContextCompat.getSystemService(this, SensorManager::class.java)
        if (sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD
            ) != null
        ) {
            Log.i(TAG, "Minimum sensors present")
            // We want to reset to auto mode on every restart, as users seem to get
            // stuck in manual mode and can't find their way out.
            // TODO(johntaylor): this is a bit of an abuse of the prefs system, but
            // the button we use is wired into the preferences system.  Should probably
            // change this to a use a different mechanism.
            sharedPreferences!!.edit().putBoolean(ApplicationConstants.AUTO_MODE_PREF_KEY, true)
                .apply()
            setAutoMode(true)
            return
        }
        // Missing at least one sensor.  Warn the user.
        handler!!.post {
            if (!sharedPreferences
                    ?.getBoolean(ApplicationConstants.NO_WARN_ABOUT_MISSING_SENSORS, false)!!
            ) {
                Log.d(TAG, "showing no sensor dialog")
                noSensorsDialogFragment!!.show(fragmentManager, "No sensors dialog")
                // First time, force manual mode.
                sharedPreferences!!.edit()
                    .putBoolean(ApplicationConstants.AUTO_MODE_PREF_KEY, false)
                    .apply()
                setAutoMode(false)
            } else {
                Log.d(TAG, "showing no sensor toast")
                Toast.makeText(
                    this@DynamicStarMapActivity, R.string.no_sensor_warning, Toast.LENGTH_LONG
                ).show()
                // Don't force manual mode second time through - leave it up to the user.
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if (fullscreenControlsManager != null) {
            fullscreenControlsManager!!.flashTheControls()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    public override fun onDestroy() {
        Log.d(TAG, "DynamicStarMap onDestroy")
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                Log.d(TAG, "Key left")
                controller!!.rotate(-10.0f)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                Log.d(TAG, "Key right")
                controller!!.rotate(10.0f)
            }
            KeyEvent.KEYCODE_BACK -> {
                // If we're in search mode when the user presses 'back' the natural
                // thing is to back out of search.
                Log.d(TAG, "In search mode $searchMode")
                if (searchMode) {
                    cancelSearch()

                }
                Log.d(TAG, "Key: $event")
                return super.onKeyDown(keyCode, event)
            }
            else -> {
                Log.d(TAG, "Key: $event")
                return super.onKeyDown(keyCode, event)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        fullscreenControlsManager!!.delayHideTheControls()
        val menuEventBundle = Bundle()
        when (item.itemId) {
            R.id.menu_item_search -> {
                Log.d(TAG, "Search")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    SEARCH_REQUESTED_LABEL
                )
                onSearchRequested()
            }
            R.id.menu_item_settings -> {
                Log.d(TAG, "Settings")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    SETTINGS_OPENED_LABEL
                )
                startActivity(Intent(this, EditSettingsActivity::class.java))
            }
            R.id.menu_item_help -> {
                Log.d(TAG, "Help")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    HELP_OPENED_LABEL
                )
                helpDialogFragment!!.show(fragmentManager!!, "Help Dialog")
            }
            R.id.menu_item_dim -> {
                Log.d(TAG, "Toggling nightmode")
                nightMode = !nightMode
                sharedPreferences!!.edit().putString(
                    ActivityLightLevelManager.LIGHT_MODE_KEY,
                    if (nightMode) "NIGHT" else "DAY"
                ).commit()
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    TOGGLED_NIGHT_MODE_LABEL
                )
            }
            R.id.menu_item_time -> {
                Log.d(TAG, "Starting Time Dialog from menu")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    TIME_TRAVEL_OPENED_LABEL
                )
                if (!timePlayerUI!!.isShown) {
                    Log.d(TAG, "Resetting time in time travel dialog.")
                    controller!!.goTimeTravel(Date())
                } else {
                    Log.d(TAG, "Resuming current time travel dialog.")
                }
                timeTravelDialogFragment!!.show(fragmentManager, "Time Travel")
            }
            R.id.menu_item_gallery -> {
                Log.d(TAG, "Loading gallery")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    GALLERY_OPENED_LABEL
                )
                startActivity(Intent(this, ImageGalleryActivity::class.java))
            }
            R.id.menu_item_tos -> {
                Log.d(TAG, "Loading ToS")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    TOS_OPENED_LABEL
                )
                eulaDialogFragmentNoButtons!!.show(fragmentManager!!, "Eula Dialog No Buttons")
            }
            R.id.menu_item_calibrate -> {
                Log.d(TAG, "Loading Calibration")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    CALIBRATION_OPENED_LABEL
                )
                val intent = Intent(this, CompassCalibrationActivity::class.java)
                intent.putExtra(CompassCalibrationActivity.HIDE_CHECKBOX, true)
                startActivity(intent)
            }
            R.id.menu_item_diagnostics -> {
                Log.d(TAG, "Loading Diagnostics")
                menuEventBundle.putString(
                    MENU_ITEM_EVENT_VALUE,
                    DIAGNOSTICS_OPENED_LABEL
                )
                startActivity(Intent(this, DiagnosticActivity::class.java))
            }
            else -> {
                Log.e(TAG, "Unwired-up menu item")
                return false
            }
        }
        analytics!!.trackEvent(MENU_ITEM_EVENT, menuEventBundle)
        return true
    }

    public override fun onStart() {
        super.onStart()
        sessionStartTime = System.currentTimeMillis()
    }

    private enum class SessionBucketLength(val seconds: Int) {
        LESS_THAN_TEN_SECS(10), TEN_SECS_TO_THIRTY_SECS(30), THIRTY_SECS_TO_ONE_MIN(60), ONE_MIN_TO_FIVE_MINS(
            300
        ),
        MORE_THAN_FIVE_MINS(
            Int.MAX_VALUE
        );
    }

    private fun getSessionLengthBucket(sessionLengthSeconds: Int): SessionBucketLength {
        for (bucket in SessionBucketLength.values()) {
            if (sessionLengthSeconds < bucket.seconds) {
                return bucket
            }
        }
        Log.e(TAG, "Programming error - should not get here")
        return SessionBucketLength.MORE_THAN_FIVE_MINS
    }

    public override fun onStop() {
        super.onStop()
        // Define a session as being the time between the main activity being in
        // the foreground and pushed back.  Note that this will mean that sessions
        // do get interrupted by (e.g.) loading preference or help screens.
        val sessionLengthSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
        val bucket = getSessionLengthBucket(sessionLengthSeconds)
        val b = Bundle()
        // Let's see how well Analytics buckets things and log the raw number
        b.putInt(SESSION_LENGTH_TIME_VALUE, sessionLengthSeconds)
        analytics!!.trackEvent(SESSION_LENGTH_EVENT, b)
    }

    public override fun onResume() {
        Log.d(TAG, "onResume at " + System.currentTimeMillis())
        super.onResume()
        Log.i(TAG, "Resuming")
        timeTravelNoise = timeTravelNoiseProvider!!.get()
        timeTravelBackNoise = timeTravelBackNoiseProvider!!.get()
        wakeLock!!.acquire()
        Log.i(TAG, "Starting view")
        skyView!!.onResume()
        Log.i(TAG, "Starting controller")
        controller!!.start()
        activityLightLevelManager!!.onResume()
        if (controller!!.isAutoMode) {
            sensorAccuracyMonitor!!.start()
        }
        for (runnable in onResumeRunnables) {
            handler!!.post(runnable)
        }
        Log.d(TAG, "-onResume at " + System.currentTimeMillis())
    }

    fun setTimeTravelMode(newTime: Date?) {
        val dateFormatter = SimpleDateFormat("yyyy.MM.dd G  HH:mm:ss z")
        Toast.makeText(
            this, String.format(
                getString(R.string.time_travel_start_message_alt),
                dateFormatter.format(newTime)
            ),
            Toast.LENGTH_LONG
        ).show()
        if (sharedPreferences!!.getBoolean(ApplicationConstants.SOUND_EFFECTS, true)) {
            try {
                timeTravelNoise!!.start()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Exception trying to play time travel sound", e)
                // It's not the end of the world - carry on.
            } catch (e: NullPointerException) {
                Log.e(TAG, "Exception trying to play time travel sound", e)
            }
        }
        Log.d(TAG, "Showing TimePlayer UI.")
        timePlayerUI!!.visibility = View.VISIBLE
        timePlayerUI!!.requestFocus()
        flashTheScreen()
        controller!!.goTimeTravel(newTime)
    }

    fun setNormalTimeModel() {
        if (sharedPreferences!!.getBoolean(ApplicationConstants.SOUND_EFFECTS, true)) {
            try {
                timeTravelBackNoise!!.start()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Exception trying to play return time travel sound", e)
                // It's not the end of the world - carry on.
            } catch (e: NullPointerException) {
                Log.e(TAG, "Exception trying to play return time travel sound", e)
            }
        }
        flashTheScreen()
        controller!!.useRealTime()
        Toast.makeText(
            this,
            R.string.time_travel_close_message,
            Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "Leaving Time Travel mode.")
        timePlayerUI!!.visibility = View.GONE
    }

    private fun flashTheScreen() {
        val view = findViewById<View>(R.id.view_mask)
        // We don't need to set it invisible again - the end of the
        // animation will see to that.
        // TODO(johntaylor): check if setting it to GONE will bring
        // performance benefits.
        view.visibility = View.VISIBLE
        view.startAnimation(flashAnimation)
    }

    public override fun onPause() {
        Log.d(TAG, "DynamicStarMap onPause")
        super.onPause()
        sensorAccuracyMonitor!!.stop()
        if (timeTravelNoise != null) {
            timeTravelNoise!!.release()
            timeTravelNoise = null
        }
        if (timeTravelBackNoise != null) {
            timeTravelBackNoise!!.release()
            timeTravelBackNoise = null
        }
        for (runnable in onResumeRunnables) {
            handler!!.removeCallbacks(runnable)
        }
        activityLightLevelManager!!.onPause()
        controller!!.stop()
        skyView!!.onPause()
        wakeLock!!.release()
        // Debug.stopMethodTracing();
        Log.d(TAG, "DynamicStarMap -onPause")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.d(TAG, "Preferences changed: key=$key")
        if (key == null) {
            return
        }
        when (key) {
            ApplicationConstants.AUTO_MODE_PREF_KEY -> {
                val autoMode = sharedPreferences.getBoolean(key, true)
                Log.d(TAG, "Automode is set to $autoMode")
                if (!autoMode) {
                    Log.d(TAG, "Switching to manual control")
                    Toast.makeText(
                        this@DynamicStarMapActivity,
                        R.string.set_manual,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d(TAG, "Switching to sensor control")
                    Toast.makeText(
                        this@DynamicStarMapActivity,
                        R.string.set_auto,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                setAutoMode(autoMode)
            }
            ApplicationConstants.ROTATE_HORIZON_PREFKEY -> {
                model!!.setHorizontalRotation(sharedPreferences.getBoolean(key, false))
                return
            }
            else -> return
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Log.d(TAG, "Touch event " + event);
        // Either of the following detectors can absorb the event, but one
        // must not hide it from the other
        var eventAbsorbed = false
        if (gestureDetector!!.onTouchEvent(event)) {
            eventAbsorbed = true
        }
        if (dragZoomRotateDetector!!.onTouchEvent(event)) {
            eventAbsorbed = true
        }
        return eventAbsorbed
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {
        // Log.d(TAG, "Trackball motion " + event);
        controller!!.rotate(event.x * ROTATION_SPEED)
        return true
    }

    private fun doSearchWithIntent(searchIntent: Intent) {
        // If we're already in search mode, cancel it.
        if (searchMode) {
            cancelSearch()
        }
        Log.d(TAG, "Performing Search")
        val queryString = searchIntent.getStringExtra(SearchManager.QUERY)
        searchMode = true
        Log.d(TAG, "Query string $queryString")
        val results = layerManager!!.searchByObjectName(
            queryString!!
        )
        val b = Bundle()
        b.putString(SEARCH_TERM, queryString)
        b.putBoolean(SEARCH_SUCCESS, results.size > 0)
        analytics!!.trackEvent(SEARCH_EVENT, b)
        if (results.isEmpty()) {
            Log.d(TAG, "No results returned")
            noSearchResultsDialogFragment!!.show(fragmentManager, "No Search Results")
        } else if (results.size > 1) {
            Log.d(TAG, "Multiple results returned")
            showUserChooseResultDialog(results)
        } else {
            Log.d(TAG, "One result returned.")
            val result = results[0]
            activateSearchTarget(result!!.coords, result.capitalizedName)
        }
    }

    private fun showUserChooseResultDialog(results: List<SearchResult?>) {
        multipleSearchResultsDialogFragment!!.clearResults()
        for (result in results) {
            multipleSearchResultsDialogFragment!!.add(result)
        }
        multipleSearchResultsDialogFragment!!.show(fragmentManager, "Multiple Search Results")
    }

    private fun initializeModelViewController() {
        Log.i(TAG, "Initializing Model, View and Controller @ " + System.currentTimeMillis())
        setContentView(R.layout.skyrenderer)
        skyView = findViewById<View>(R.id.skyrenderer_view) as GLSurfaceView
        // We don't want a depth buffer.
        skyView!!.setEGLConfigChooser(false)
        val renderer = SkyRenderer(resources)
        skyView!!.setRenderer(renderer)
        rendererController = RendererController(renderer, skyView!!)
        // The renderer will now call back every frame to get model updates.
        rendererController!!.addUpdateClosure(
            RendererModelUpdateClosure(model, rendererController!!, sharedPreferences)
        )
        Log.i(TAG, "Setting layers @ " + System.currentTimeMillis())
        layerManager!!.registerWithRenderer(rendererController)
        Log.i(TAG, "Set up controllers @ " + System.currentTimeMillis())
        controller!!.setModel(model)
        wireUpScreenControls() // TODO(johntaylor) move these?
        wireUpTimePlayer() // TODO(widdows) move these?
    }

    private fun setAutoMode(auto: Boolean) {
        val b = Bundle()
        b.putString(MENU_ITEM_EVENT_VALUE, TOGGLED_MANUAL_MODE_LABEL)
        controller!!.isAutoMode = auto
        if (auto) {
            sensorAccuracyMonitor!!.start()
        } else {
            sensorAccuracyMonitor!!.stop()
        }
    }

    private fun wireUpScreenControls() {
        cancelSearchButton = findViewById<View>(R.id.cancel_search_button) as ImageButton
        // TODO(johntaylor): move to set this in the XML once we don't support 1.5
        cancelSearchButton!!.setOnClickListener { cancelSearch() }
        val providerButtons = findViewById<View>(R.id.layer_buttons_control) as ButtonLayerView
        val numChildren = providerButtons.childCount
        val buttonViews: MutableList<View> = ArrayList()
        for (i in 0 until numChildren) {
            val button = providerButtons.getChildAt(i) as ImageButton
            buttonViews.add(button)
        }
        buttonViews.add(findViewById(R.id.manual_auto_toggle))
        val manualButtonLayer = findViewById<View>(
            R.id.layer_manual_auto_toggle
        ) as ButtonLayerView
        fullscreenControlsManager = FullscreenControlsManager(
            this,
            findViewById(R.id.main_sky_view),
            asList<View>(manualButtonLayer, providerButtons),
            buttonViews
        )
        val mapMover = MapMover(model!!, controller!!, this)
        gestureDetector = GestureDetector(
            this, GestureInterpreter(
                fullscreenControlsManager!!, mapMover
            )
        )
        dragZoomRotateDetector = DragRotateZoomGestureDetector(mapMover)
    }

    private fun cancelSearch() {
        val searchControlBar = findViewById<View>(R.id.search_control_bar)
        searchControlBar.visibility = View.INVISIBLE
        rendererController!!.queueDisableSearchOverlay()
        searchMode = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "New Intent received $intent")
        if (Intent.ACTION_SEARCH == intent.action) {
            doSearchWithIntent(intent)
        }
    }

    override fun onRestoreInstanceState(icicle: Bundle) {
        Log.d(TAG, "DynamicStarMap onRestoreInstanceState")
        super.onRestoreInstanceState(icicle)
        if (icicle == null) return
        searchMode = icicle.getBoolean(ApplicationConstants.BUNDLE_SEARCH_MODE)
        val x = icicle.getFloat(ApplicationConstants.BUNDLE_X_TARGET)
        val y = icicle.getFloat(ApplicationConstants.BUNDLE_Y_TARGET)
        val z = icicle.getFloat(ApplicationConstants.BUNDLE_Z_TARGET)
        searchTarget = Vector3(x, y, z)
        searchTargetName = icicle.getString(ApplicationConstants.BUNDLE_TARGET_NAME)
        if (searchMode) {
            Log.d(TAG, "Searching for target $searchTargetName at target=$searchTarget")
            rendererController!!.queueEnableSearchOverlay(searchTarget, searchTargetName)
            cancelSearchButton!!.visibility = View.VISIBLE
        }
        nightMode = icicle.getBoolean(ApplicationConstants.BUNDLE_NIGHT_MODE, false)
    }

    override fun onSaveInstanceState(icicle: Bundle) {
        Log.d(TAG, "DynamicStarMap onSaveInstanceState")
        icicle.putBoolean(ApplicationConstants.BUNDLE_SEARCH_MODE, searchMode)
        icicle.putFloat(ApplicationConstants.BUNDLE_X_TARGET, searchTarget!!.x)
        icicle.putFloat(ApplicationConstants.BUNDLE_Y_TARGET, searchTarget!!.y)
        icicle.putFloat(ApplicationConstants.BUNDLE_Z_TARGET, searchTarget!!.z)
        icicle.putString(ApplicationConstants.BUNDLE_TARGET_NAME, searchTargetName)
        icicle.putBoolean(ApplicationConstants.BUNDLE_NIGHT_MODE, nightMode)
        super.onSaveInstanceState(icicle)
    }

    fun activateSearchTarget(target: Vector3?, searchTerm: String) {
        Log.d(TAG, "Item $searchTerm selected")
        // Store these for later.
        searchTarget = target
        searchTargetName = searchTerm
        Log.d(TAG, "Searching for target=$target")
        rendererController!!.queueViewerUpDirection(model!!.zenith.copyForJ())
        rendererController!!.queueEnableSearchOverlay(target!!.copyForJ(), searchTerm)
        val autoMode = sharedPreferences!!.getBoolean(ApplicationConstants.AUTO_MODE_PREF_KEY, true)
        if (!autoMode) {
            controller!!.teleport(target)
        }
        val searchPromptText = findViewById<View>(R.id.search_status_label) as TextView
        searchPromptText.text =
            String.format("%s %s", getString(R.string.search_target_looking_message), searchTerm)
        val searchControlBar = findViewById<View>(R.id.search_control_bar)
        searchControlBar.visibility = View.VISIBLE
    }

    /**
     * Creates and wire up all time player controls.
     */
    private fun wireUpTimePlayer() {
        Log.d(TAG, "Initializing TimePlayer UI.")
        timePlayerUI = findViewById(R.id.time_player_view)
        val timePlayerCancelButton = findViewById<View>(R.id.time_player_close) as ImageButton
        val timePlayerBackwardsButton = findViewById<View>(
            R.id.time_player_play_backwards
        ) as ImageButton
        val timePlayerStopButton = findViewById<View>(R.id.time_player_play_stop) as ImageButton
        val timePlayerForwardsButton = findViewById<View>(
            R.id.time_player_play_forwards
        ) as ImageButton
        val timeTravelSpeedLabel = findViewById<View>(R.id.time_travel_speed_label) as TextView
        timePlayerCancelButton.setOnClickListener {
            Log.d(TAG, "Heard time player close click.")
            setNormalTimeModel()
        }
        timePlayerBackwardsButton.setOnClickListener {
            Log.d(TAG, "Heard time player play backwards click.")
            controller!!.decelerateTimeTravel()
            timeTravelSpeedLabel.setText(controller!!.currentSpeedTag)
        }
        timePlayerStopButton.setOnClickListener {
            Log.d(TAG, "Heard time player play stop click.")
            controller!!.pauseTime()
            timeTravelSpeedLabel.setText(controller!!.currentSpeedTag)
        }
        timePlayerForwardsButton.setOnClickListener {
            Log.d(TAG, "Heard time player play forwards click.")
            controller!!.accelerateTimeTravel()
            timeTravelSpeedLabel.setText(controller!!.currentSpeedTag)
        }
        val displayUpdater: Runnable = object : Runnable {
            private val timeTravelTimeReadout = findViewById<View>(
                R.id.time_travel_time_readout
            ) as TextView
            private val timeTravelStatusLabel = findViewById<View>(
                R.id.time_travel_status_label
            ) as TextView
            private val timeTravelSpeedLabel = findViewById<View>(
                R.id.time_travel_speed_label
            ) as TextView
            private val dateFormatter = SimpleDateFormat(
                "yyyy.MM.dd G  HH:mm:ss z"
            )
            private val date = Date()
            override fun run() {
                val time = model!!.timeMillis
                date.time = time
                timeTravelTimeReadout.text = dateFormatter.format(date)
                if (time > System.currentTimeMillis()) {
                    timeTravelStatusLabel.setText(R.string.time_travel_label_future)
                } else {
                    timeTravelStatusLabel.setText(R.string.time_travel_label_past)
                }
                timeTravelSpeedLabel.setText(controller!!.currentSpeedTag)
                handler!!.postDelayed(this, TIME_DISPLAY_DELAY_MILLIS.toLong())
            }
        }
        onResumeRunnables.add(displayUpdater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == GOOGLE_PLAY_SERVICES_REQUEST_CODE) {
            playServicesChecker!!.runAfterDialog()
            return
        }
        Log.w(TAG, "Unhandled activity result")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == GOOGLE_PLAY_SERVICES_REQUEST_LOCATION_PERMISSION_CODE) {
            playServicesChecker!!.runAfterPermissionsCheck(requestCode, permissions, grantResults)
            return
        }
        Log.w(TAG, "Unhandled request permissions result")
    }

    companion object {
        private const val TIME_DISPLAY_DELAY_MILLIS = 1000

        // Activity for result Ids
        const val GOOGLE_PLAY_SERVICES_REQUEST_CODE = 1
        const val GOOGLE_PLAY_SERVICES_REQUEST_LOCATION_PERMISSION_CODE = 2

        // End Activity for result Ids
        private const val ROTATION_SPEED = 10f
        private val TAG = getTag(DynamicStarMapActivity::class.java)
    }
}