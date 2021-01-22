/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.MultipleSearchResultsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NoSearchResultsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.NoSensorsDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.TimeTravelDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelChanger;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelManager;
import io.github.marcocipriani01.telescopetouch.activities.util.FullscreenControlsManager;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.control.ControllerGroup;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;
import io.github.marcocipriani01.telescopetouch.layers.LayerManager;
import io.github.marcocipriani01.telescopetouch.renderer.RendererController;
import io.github.marcocipriani01.telescopetouch.renderer.SkyRenderer;
import io.github.marcocipriani01.telescopetouch.renderer.util.AbstractUpdateClosure;
import io.github.marcocipriani01.telescopetouch.search.SearchResult;
import io.github.marcocipriani01.telescopetouch.touch.DragRotateZoomGestureDetector;
import io.github.marcocipriani01.telescopetouch.touch.GestureInterpreter;
import io.github.marcocipriani01.telescopetouch.touch.MapMover;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.units.Vector3;
import io.github.marcocipriani01.telescopetouch.util.SensorAccuracyMonitor;
import io.github.marcocipriani01.telescopetouch.views.ButtonLayerView;

/**
 * The main map-rendering Activity.
 */
public class DynamicStarMapActivity extends InjectableActivity
        implements OnSharedPreferenceChangeListener, HasComponent<DynamicStarMapComponent> {

    public static final String SKY_MAP_INTENT_ACTION = "io.github.marcocipriani01.telescopetouch.activities.DynamicStarMapActivity";
    private static final int TIME_DISPLAY_DELAY_MILLIS = 1000;
    private static final float ROTATION_SPEED = 10;
    private static final String TAG = TelescopeTouchApp.getTag(DynamicStarMapActivity.class);
    // A list of runnables to post on the handler when we resume.
    private final List<Runnable> onResumeRunnables = new ArrayList<>();
    // End Activity for result Ids
    @Inject
    ControllerGroup controller;
    @Inject
    AstronomerModel model;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    LayerManager layerManager;
    @Inject
    Handler handler;
    @Inject
    FragmentManager fragmentManager;
    @Inject
    TimeTravelDialogFragment timeTravelDialogFragment;
    @Inject
    NoSearchResultsDialogFragment noSearchResultsDialogFragment;
    @Inject
    MultipleSearchResultsDialogFragment multipleSearchResultsDialogFragment;
    @Inject
    NoSensorsDialogFragment noSensorsDialogFragment;
    @Inject
    SensorAccuracyMonitor sensorAccuracyMonitor;
    // We need to maintain references to these objects to keep them from getting gc'd.
    //@Inject
    //@SuppressWarnings("unused")
    //MagneticDeclinationCalculatorSwitcher magneticSwitcher;
    @Inject
    Animation flashAnimation;
    private FullscreenControlsManager fullscreenControlsManager;
    private ImageButton cancelSearchButton;
    private GestureDetector gestureDetector;
    private RendererController rendererController;
    private boolean nightMode = false;
    private boolean searchMode = false;
    private GeocentricCoordinates searchTarget = GeocentricCoordinates.getInstance(0, 0);
    private GLSurfaceView skyView;
    private String searchTargetName;
    private View timePlayerUI;
    private DynamicStarMapComponent daggerComponent;
    private DragRotateZoomGestureDetector dragZoomRotateDetector;
    private ActivityLightLevelManager activityLightLevelManager;
    private boolean isSkyMapOnly;
    private SearchView searchView;
    private MenuItem searchMenuItem;

    @Override
    public DynamicStarMapComponent getComponent() {
        return daggerComponent;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle icicle) {
        Log.d(TAG, "onCreate at " + System.currentTimeMillis());
        super.onCreate(icicle);

        daggerComponent = DaggerDynamicStarMapComponent.builder()
                .applicationComponent(getApplicationComponent())
                .dynamicStarMapModule(new DynamicStarMapModule(this)).build();
        daggerComponent.inject(this);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);


        // Set up full screen mode, hide the system UI etc.
        Window window = getWindow();
        //if (Build.VERSION.SDK_INT < 30) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        //} else {
        //    window.setDecorFitsSystemWindows(false);
        //    WindowInsetsController controller = window.getInsetsController();
        //    if (controller != null) {
        //        controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        //        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        //    }
        //}

        initializeModelViewController();
        checkForSensorsAndMaybeWarn();

        // Search related
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        ActivityLightLevelChanger activityLightLevelChanger = new ActivityLightLevelChanger(this,
                nightMode1 -> DynamicStarMapActivity.this.rendererController.queueNightVisionMode(nightMode1));
        activityLightLevelManager = new ActivityLightLevelManager(activityLightLevelChanger,
                sharedPreferences);

        String intentAction = getIntent().getAction();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            isSkyMapOnly = ((intentAction != null) && (intentAction.equals(SKY_MAP_INTENT_ACTION)));
            actionBar.setDisplayHomeAsUpEnabled(!isSkyMapOnly);
            actionBar.setDisplayShowHomeEnabled(!isSkyMapOnly);
        }
        this.<ImageButton>findViewById(R.id.back_telescope_control).setOnClickListener(v -> {
            if (isSkyMapOnly) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                onBackPressed();
            }
        });
        this.<Button>findViewById(R.id.search_in_database).setOnClickListener(v -> {
            Intent intent = new Intent(DynamicStarMapActivity.this, MainActivity.class);
            if (TelescopeTouchApp.connectionManager.isConnected()) {
                GoToFragment.setRequestedSearch(searchTargetName);
                intent.putExtra(MainActivity.ACTION, MainActivity.ACTION_SEARCH);
            } else {
                intent.putExtra(MainActivity.ACTION, MainActivity.ACTION_CONNECT);
                Toast.makeText(DynamicStarMapActivity.this, R.string.connect_telescope_first, Toast.LENGTH_SHORT).show();
            }
            startActivity(intent);
        });

        // Were we started as the result of a search?
        Intent intent = getIntent();
        Log.d(TAG, "Intent received: " + intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d(TAG, "Started as a result of a search");
            doSearchWithIntent(intent);
        }
    }

    private void checkForSensorsAndMaybeWarn() {
        SensorManager sensorManager = ContextCompat.getSystemService(this, SensorManager.class);
        if (sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            Log.i(TAG, "Minimum sensors present");
            sharedPreferences.edit().putBoolean(ApplicationConstants.AUTO_MODE_PREF_KEY, true).apply();
            setAutoMode(true);
            return;
        }
        // Missing at least one sensor.  Warn the user.
        handler.post(() -> {
            if (!sharedPreferences
                    .getBoolean(ApplicationConstants.NO_WARN_ABOUT_MISSING_SENSORS, false)) {
                Log.d(TAG, "showing no sensor dialog");
                noSensorsDialogFragment.show(fragmentManager, "No sensors dialog");
                // First time, force manual mode.
                sharedPreferences.edit().putBoolean(ApplicationConstants.AUTO_MODE_PREF_KEY, false)
                        .apply();
                setAutoMode(false);
            } else {
                Log.d(TAG, "showing no sensor toast");
                Toast.makeText(
                        DynamicStarMapActivity.this, R.string.no_sensor_warning, Toast.LENGTH_LONG).show();
                // Don't force manual mode second time through - leave it up to the user.
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if (fullscreenControlsManager != null) {
            fullscreenControlsManager.flashTheControls();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.skymap, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.menu_skymap_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "DynamicStarMap onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case (KeyEvent.KEYCODE_DPAD_LEFT):
                Log.d(TAG, "Key left");
                controller.rotate(-10.0f);
                break;
            case (KeyEvent.KEYCODE_DPAD_RIGHT):
                Log.d(TAG, "Key right");
                controller.rotate(10.0f);
                break;
            case (KeyEvent.KEYCODE_BACK):
                // If we're in search mode when the user presses 'back' the natural
                // thing is to back out of search.
                Log.d(TAG, "In search mode " + searchMode);
                if (searchMode) {
                    cancelSearch();
                    break;
                }
            default:
                Log.d(TAG, "Key: " + event);
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        fullscreenControlsManager.delayHideTheControls();
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.menu_skymap_search) {
            Log.d(TAG, "Search");
            onSearchRequested();
        } else if (itemId == R.id.menu_skymap_settings) {
            Log.d(TAG, "Settings");
            startActivity(new Intent(this, EditSettingsActivity.class));
        } else if (itemId == R.id.menu_skymap_red) {
            Log.d(TAG, "Toggling nightmode");
            nightMode = !nightMode;
            sharedPreferences.edit().putString(ActivityLightLevelManager.LIGHT_MODE_KEY,
                    nightMode ? "NIGHT" : "DAY").apply();
        } else if (itemId == R.id.menu_skymap_time_travel) {
            Log.d(TAG, "Starting Time Dialog from menu");
            if (!timePlayerUI.isShown()) {
                Log.d(TAG, "Resetting time in time travel dialog.");
                controller.goTimeTravel(new Date());
            } else {
                Log.d(TAG, "Resuming current time travel dialog.");
            }
            timeTravelDialogFragment.show(fragmentManager, "Time Travel");
        } else if (itemId == R.id.menu_skymap_gallery) {
            Log.d(TAG, "Loading gallery");
            startActivity(new Intent(this, ImageGalleryActivity.class));
        } else if (itemId == R.id.menu_skymap_calibrate) {
            Log.d(TAG, "Loading Calibration");
            Intent intent = new Intent(this, CompassCalibrationActivity.class);
            intent.putExtra(CompassCalibrationActivity.HIDE_CHECKBOX, true);
            startActivity(intent);
        } else if (itemId == R.id.menu_skymap_diagnostics) {
            Log.d(TAG, "Loading Diagnostics");
            startActivity(new Intent(this, DiagnosticActivity.class));
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume at " + System.currentTimeMillis());
        super.onResume();
        Log.i(TAG, "Starting view");
        skyView.onResume();
        Log.i(TAG, "Starting controller");
        controller.start();
        activityLightLevelManager.onResume();
        if (controller.isAutoMode()) {
            sensorAccuracyMonitor.start();
        }
        for (Runnable runnable : onResumeRunnables) {
            handler.post(runnable);
        }
        Log.d(TAG, "-onResume at " + System.currentTimeMillis());
    }

    @SuppressLint("SimpleDateFormat")
    public void setTimeTravelMode(Date newTime) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd G  HH:mm:ss z");
        Toast.makeText(this, String.format(getString(R.string.time_travel_start_message_alt),
                dateFormatter.format(newTime)), Toast.LENGTH_LONG).show();
        Log.d(TAG, "Showing TimePlayer UI.");
        timePlayerUI.setVisibility(View.VISIBLE);
        timePlayerUI.requestFocus();
        flashTheScreen();
        controller.goTimeTravel(newTime);
    }

    public void setNormalTimeModel() {
        flashTheScreen();
        controller.useRealTime();
        Toast.makeText(this,
                R.string.time_travel_close_message,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Leaving Time Travel mode.");
        timePlayerUI.setVisibility(View.GONE);
    }

    private void flashTheScreen() {
        final View view = findViewById(R.id.view_mask);
        // We don't need to set it invisible again - the end of the
        // animation will see to that.
        // TODO(johntaylor): check if setting it to GONE will bring
        // performance benefits.
        view.setVisibility(View.VISIBLE);
        view.startAnimation(flashAnimation);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "DynamicStarMap onPause");
        super.onPause();
        sensorAccuracyMonitor.stop();
        for (Runnable runnable : onResumeRunnables) {
            handler.removeCallbacks(runnable);
        }
        activityLightLevelManager.onPause();
        controller.stop();
        skyView.onPause();
        Log.d(TAG, "DynamicStarMap -onPause");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preferences changed: key=" + key);
        if (ApplicationConstants.AUTO_MODE_PREF_KEY.equals(key)) {
            setAutoMode(sharedPreferences.getBoolean(key, true));
        } else if (ApplicationConstants.ROTATE_HORIZON_PREFKEY.equals(key)) {
            model.setHorizontalRotation(sharedPreferences.getBoolean(key, false));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Log.d(TAG, "Touch event " + event);
        // Either of the following detectors can absorb the event, but one
        // must not hide it from the other
        boolean eventAbsorbed = false;
        if (gestureDetector.onTouchEvent(event)) {
            eventAbsorbed = true;
        }
        if (dragZoomRotateDetector.onTouchEvent(event)) {
            eventAbsorbed = true;
        }
        return eventAbsorbed;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        // Log.d(TAG, "Trackball motion " + event);
        controller.rotate(event.getX() * ROTATION_SPEED);
        return true;
    }

    private void doSearchWithIntent(Intent searchIntent) {
        // If we're already in search mode, cancel it.
        if (searchMode) {
            cancelSearch();
        }
        Log.d(TAG, "Performing Search");
        final String queryString = searchIntent.getStringExtra(SearchManager.QUERY);
        searchMode = true;
        Log.d(TAG, "Query string " + queryString);
        List<SearchResult> results = layerManager.searchByObjectName(queryString);
        if (results.isEmpty()) {
            Log.d(TAG, "No results returned");
            noSearchResultsDialogFragment.show(fragmentManager, "No Search Results");
        } else if (results.size() > 1) {
            Log.d(TAG, "Multiple results returned");
            showUserChooseResultDialog(results);
        } else {
            Log.d(TAG, "One result returned.");
            final SearchResult result = results.get(0);
            activateSearchTarget(result.coords, result.capitalizedName);
        }
    }

    private void showUserChooseResultDialog(List<SearchResult> results) {
        multipleSearchResultsDialogFragment.clearResults();
        for (SearchResult result : results) {
            multipleSearchResultsDialogFragment.add(result);
        }
        multipleSearchResultsDialogFragment.show(fragmentManager, "Multiple Search Results");
    }

    @SuppressLint("SimpleDateFormat")
    private void initializeModelViewController() {
        Log.i(TAG, "Initializing Model, View and Controller @ " + System.currentTimeMillis());
        setContentView(R.layout.skyrenderer);
        skyView = findViewById(R.id.skyrenderer_view);
        // We don't want a depth buffer.
        skyView.setEGLConfigChooser(false);
        SkyRenderer renderer = new SkyRenderer(getResources());
        skyView.setRenderer(renderer);

        rendererController = new RendererController(renderer, skyView);
        // The renderer will now call back every frame to get model updates.
        rendererController.addUpdateClosure(
                new RendererModelUpdateClosure(model, rendererController, sharedPreferences));

        Log.i(TAG, "Setting layers @ " + System.currentTimeMillis());
        layerManager.registerWithRenderer(rendererController);
        Log.i(TAG, "Set up controllers @ " + System.currentTimeMillis());
        controller.setModel(model);

        cancelSearchButton = findViewById(R.id.cancel_search_button);
        cancelSearchButton.setOnClickListener(v1 -> cancelSearch());

        ButtonLayerView providerButtons = findViewById(R.id.layer_buttons_control);

        int numChildren = providerButtons.getChildCount();
        List<View> buttonViews = new ArrayList<>();
        for (int i = 0; i < numChildren; ++i) {
            ImageButton button = (ImageButton) providerButtons.getChildAt(i);
            buttonViews.add(button);
        }
        buttonViews.add(findViewById(R.id.manual_auto_toggle));
        ButtonLayerView manualButtonLayer = findViewById(R.id.layer_manual_auto_toggle);

        fullscreenControlsManager = new FullscreenControlsManager(this, findViewById(R.id.main_sky_view),
                Arrays.asList(manualButtonLayer, providerButtons), buttonViews);

        MapMover mapMover = new MapMover(model, controller, this);

        gestureDetector = new GestureDetector(this, new GestureInterpreter(fullscreenControlsManager, mapMover));
        dragZoomRotateDetector = new DragRotateZoomGestureDetector(mapMover);

        Log.d(TAG, "Initializing TimePlayer UI.");
        timePlayerUI = findViewById(R.id.time_player_view);
        ImageButton timePlayerCancelButton = findViewById(R.id.time_player_close);
        ImageButton timePlayerBackwardsButton = findViewById(
                R.id.time_player_play_backwards);
        ImageButton timePlayerStopButton = findViewById(R.id.time_player_play_stop);
        ImageButton timePlayerForwardsButton = findViewById(
                R.id.time_player_play_forwards);
        final TextView timeTravelSpeedLabel = findViewById(R.id.time_travel_speed_label);

        timePlayerCancelButton.setOnClickListener(v -> {
            Log.d(TAG, "Heard time player close click.");
            setNormalTimeModel();
        });
        timePlayerBackwardsButton.setOnClickListener(v -> {
            Log.d(TAG, "Heard time player play backwards click.");
            controller.decelerateTimeTravel();
            timeTravelSpeedLabel.setText(controller.getCurrentSpeedTag());
        });
        timePlayerStopButton.setOnClickListener(v -> {
            Log.d(TAG, "Heard time player play stop click.");
            controller.pauseTime();
            timeTravelSpeedLabel.setText(controller.getCurrentSpeedTag());
        });
        timePlayerForwardsButton.setOnClickListener(v -> {
            Log.d(TAG, "Heard time player play forwards click.");
            controller.accelerateTimeTravel();
            timeTravelSpeedLabel.setText(controller.getCurrentSpeedTag());
        });

        Runnable displayUpdater = new Runnable() {
            private final TextView timeTravelTimeReadout = findViewById(R.id.time_travel_time_readout);
            private final TextView timeTravelStatusLabel = findViewById(R.id.time_travel_status_label);
            private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd G  HH:mm:ss z");
            private final Date date = new Date();

            @Override
            public void run() {
                long time = model.getTimeMillis();
                date.setTime(time);
                timeTravelTimeReadout.setText(dateFormatter.format(date));
                if (time > System.currentTimeMillis()) {
                    timeTravelStatusLabel.setText(R.string.time_travel_label_future);
                } else {
                    timeTravelStatusLabel.setText(R.string.time_travel_label_past);
                }
                timeTravelSpeedLabel.setText(controller.getCurrentSpeedTag());
                handler.postDelayed(this, TIME_DISPLAY_DELAY_MILLIS);
            }
        };
        onResumeRunnables.add(displayUpdater);
    }

    private void setAutoMode(boolean auto) {
        controller.setAutoMode(auto);
        if (auto) {
            sensorAccuracyMonitor.start();
        } else {
            sensorAccuracyMonitor.stop();
        }
    }

    private void cancelSearch() {
        View searchControlBar = findViewById(R.id.search_control_bar);
        searchControlBar.setVisibility(View.INVISIBLE);
        rendererController.queueDisableSearchOverlay();
        searchMode = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "New Intent received " + intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            doSearchWithIntent(intent);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        Log.d(TAG, "DynamicStarMap onRestoreInstanceState");
        super.onRestoreInstanceState(icicle);
        if (icicle == null) return;
        searchMode = icicle.getBoolean(ApplicationConstants.BUNDLE_SEARCH_MODE);
        float x = icicle.getFloat(ApplicationConstants.BUNDLE_X_TARGET);
        float y = icicle.getFloat(ApplicationConstants.BUNDLE_Y_TARGET);
        float z = icicle.getFloat(ApplicationConstants.BUNDLE_Z_TARGET);
        searchTarget = new GeocentricCoordinates(x, y, z);
        searchTargetName = icicle.getString(ApplicationConstants.BUNDLE_TARGET_NAME);
        if (searchMode) {
            Log.d(TAG, "Searching for target " + searchTargetName + " at target=" + searchTarget);
            rendererController.queueEnableSearchOverlay(searchTarget, searchTargetName);
            cancelSearchButton.setVisibility(View.VISIBLE);
        }
        nightMode = icicle.getBoolean(ApplicationConstants.BUNDLE_NIGHT_MODE, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        Log.d(TAG, "DynamicStarMap onSaveInstanceState");
        icicle.putBoolean(ApplicationConstants.BUNDLE_SEARCH_MODE, searchMode);
        icicle.putFloat(ApplicationConstants.BUNDLE_X_TARGET, searchTarget.x);
        icicle.putFloat(ApplicationConstants.BUNDLE_Y_TARGET, searchTarget.y);
        icicle.putFloat(ApplicationConstants.BUNDLE_Z_TARGET, searchTarget.z);
        icicle.putString(ApplicationConstants.BUNDLE_TARGET_NAME, searchTargetName);
        icicle.putBoolean(ApplicationConstants.BUNDLE_NIGHT_MODE, nightMode);
        super.onSaveInstanceState(icicle);
    }

    public void activateSearchTarget(GeocentricCoordinates target, final String searchTerm) {
        Log.d(TAG, "Item " + searchTerm + " selected");
        if (searchView != null) {
            searchView.clearFocus();
            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
        }
        if (searchMenuItem != null)
            searchMenuItem.collapseActionView();
        // Store these for later.
        searchTarget = target;
        searchTargetName = searchTerm;
        Log.d(TAG, "Searching for target=" + target);
        rendererController.queueViewerUpDirection(model.getZenith().copy());
        rendererController.queueEnableSearchOverlay(target.copy(), searchTerm);
        boolean autoMode = sharedPreferences.getBoolean(ApplicationConstants.AUTO_MODE_PREF_KEY, true);
        if (!autoMode) {
            controller.teleport(target);
        }

        TextView searchPromptText = findViewById(R.id.search_status_label);
        searchPromptText.setText(
                String.format("%s %s", getString(R.string.search_target_looking_message), searchTerm));
        View searchControlBar = findViewById(R.id.search_control_bar);
        searchControlBar.setVisibility(View.VISIBLE);
    }

    public AstronomerModel getModel() {
        return model;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG, "Unhandled activity result");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.w(TAG, "Unhandled request permissions result");
    }

    /**
     * Passed to the renderer to get per-frame updates from the model.
     *
     * @author John Taylor
     */
    private static final class RendererModelUpdateClosure extends AbstractUpdateClosure {
        private final RendererController rendererController;
        private final AstronomerModel model;

        public RendererModelUpdateClosure(AstronomerModel model,
                                          RendererController rendererController, SharedPreferences sharedPreferences) {
            this.model = model;
            this.rendererController = rendererController;
            boolean horizontalRotation = sharedPreferences.getBoolean(ApplicationConstants.ROTATE_HORIZON_PREFKEY, false);
            model.setHorizontalRotation(horizontalRotation);
        }

        @Override
        public void run() {
            AstronomerModel.Pointing pointing = model.getPointing();
            float directionX = pointing.getLineOfSightX();
            float directionY = pointing.getLineOfSightY();
            float directionZ = pointing.getLineOfSightZ();

            float upX = pointing.getPerpendicularX();
            float upY = pointing.getPerpendicularY();
            float upZ = pointing.getPerpendicularZ();

            rendererController.queueSetViewOrientation(directionX, directionY, directionZ, upX, upY, upZ);

            Vector3 up = model.getPhoneUpDirection();
            rendererController.queueTextAngle((float) Math.atan2(up.x, up.y));
            rendererController.queueViewerUpDirection(model.getZenith().copy());

            float fieldOfView = model.getFieldOfView();
            rendererController.queueFieldOfView(fieldOfView);
        }
    }
}
