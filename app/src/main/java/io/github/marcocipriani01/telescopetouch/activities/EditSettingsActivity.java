package io.github.marcocipriani01.telescopetouch.activities;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelChanger;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelManager;

/**
 * Edit the user's preferences.
 */
public class EditSettingsActivity extends AppCompatActivity {

    /**
     * These must match the keys in the preference_screen.xml file.
     */
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String LOCATION = "location";
    private static final String TAG = TelescopeTouchApp.getTag(EditSettingsActivity.class);
    @Inject
    SharedPreferences sharedPreferences;
    private MyPreferenceFragment preferenceFragment;
    private Geocoder geocoder;
    private ActivityLightLevelManager activityLightLevelManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TelescopeTouchApp) getApplication()).getApplicationComponent().inject(this);
        activityLightLevelManager = new ActivityLightLevelManager(
                new ActivityLightLevelChanger(this, null),
                PreferenceManager.getDefaultSharedPreferences(this));
        geocoder = new Geocoder(this);
        preferenceFragment = new MyPreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                preferenceFragment).commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final Preference locationPreference = preferenceFragment.findPreference(LOCATION);
        Preference latitudePreference = preferenceFragment.findPreference(LATITUDE);
        Preference longitudePreference = preferenceFragment.findPreference(LONGITUDE);
        Objects.requireNonNull(locationPreference).setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d(TAG, "Place to be updated to " + newValue);
            return setLatLongFromPlace(newValue.toString());
        });
        Objects.requireNonNull(latitudePreference).setOnPreferenceChangeListener((preference, newValue) -> {
            ((EditTextPreference) locationPreference).setText("");
            return true;
        });
        Objects.requireNonNull(longitudePreference).setOnPreferenceChangeListener((preference, newValue) -> {
            ((EditTextPreference) locationPreference).setText("");
            return true;
        });
        Preference gyroPreference = preferenceFragment.findPreference(
                ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO);
        Objects.requireNonNull(gyroPreference).setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d(TAG, "Toggling gyro preference " + newValue);
            enableNonGyroSensorPrefs(((Boolean) newValue));
            return true;
        });
        enableNonGyroSensorPrefs(
                sharedPreferences.getBoolean(ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO,
                        false));
    }

    @Override
    public void onResume() {
        super.onResume();
        activityLightLevelManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePreferences();
        activityLightLevelManager.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableNonGyroSensorPrefs(boolean enabled) {
        Objects.<Preference>requireNonNull(preferenceFragment.findPreference(ApplicationConstants.REVERSE_MAGNETIC_Z_PREFKEY))
                .setEnabled(enabled);
    }

    /**
     * Updates preferences on singletons, so we don't have to register
     * preference change listeners for them.
     */
    private void updatePreferences() {
        Log.d(TAG, "Updating preferences");
    }

    protected boolean setLatLongFromPlace(String place) {
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(place, 1);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.location_unable_to_geocode), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (addresses.isEmpty()) {
            showNotFoundDialog(place);
            return false;
        }
        // TODO(johntaylor) let the user choose, but for now just pick the first.
        Address first = addresses.get(0);
        setLatLong(first.getLatitude(), first.getLongitude());
        return true;
    }

    private void setLatLong(double latitude, double longitude) {
        EditTextPreference latPreference = preferenceFragment.findPreference(LATITUDE);
        EditTextPreference longPreference = preferenceFragment.findPreference(LONGITUDE);
        Objects.requireNonNull(latPreference).setText(Double.toString(latitude));
        Objects.requireNonNull(longPreference).setText(Double.toString(longitude));
        String message = String.format(getString(R.string.location_place_found), latitude, longitude);
        Log.d(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showNotFoundDialog(String place) {
        String message = String.format(getString(R.string.location_not_found), place);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.location_not_found_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preference_screen);
        }
    }
}