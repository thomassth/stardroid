package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 15;
    private static final int REQUEST_ACCESS_LOCATION = 1;
    private static final String BUNDLE_CAMERA = "camera_position";
    private static final String BUNDLE_MARKER = "marker";
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private CameraPosition cameraPosition;
    private LatLng marker;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(BUNDLE_CAMERA);
            marker = savedInstanceState.getParcelable(BUNDLE_MARKER);
        }
        setContentView(R.layout.activity_maps);
        fab = this.findViewById(R.id.maps_done_fab);
        fab.setOnClickListener(v -> finish());
        fab.hide();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        if (marker == null) {
            setResult(Activity.RESULT_CANCELED, intent);
        } else {
            intent.putExtra(ApplicationConstants.LATITUDE_PREF, Double.toString(marker.latitude));
            intent.putExtra(ApplicationConstants.LONGITUDE_PREF, Double.toString(marker.longitude));
            setResult(Activity.RESULT_OK, intent);
        }
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(BUNDLE_CAMERA, map.getCameraPosition());
            outState.putParcelable(BUNDLE_MARKER, marker);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        getLocationPermission();
        updateLocationUI();
        if (this.cameraPosition != null)
            map.moveCamera(CameraUpdateFactory.newCameraPosition(this.cameraPosition));
        if (marker != null) {
            map.addMarker(getMarker());
            fab.show();
        } else {
            getDeviceLocation();
        }
        map.setOnMapClickListener(newLatLon -> {
            map.clear();
            marker = newLatLon;
            map.addMarker(getMarker());
            fab.show();
        });
        map.setOnMarkerClickListener(marker -> {
            finish();
            return false;
        });
    }

    private MarkerOptions getMarker() {
        return new MarkerOptions().position(marker).title("Select position");
    }

    private void getDeviceLocation() {
        if (locationPermissionGranted) {
            try {
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Location result = task.getResult();
                        if (result != null)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(result.getLatitude(), result.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage(), e);
            }
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = (requestCode == REQUEST_ACCESS_LOCATION) &&
                (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) return;
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}