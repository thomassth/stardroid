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

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelChanger;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelManager;
import io.github.marcocipriani01.telescopetouch.gallery.GalleryFactory;
import io.github.marcocipriani01.telescopetouch.gallery.GalleryImage;

/**
 * Shows an image to the user and allows them to search for it.
 *
 * @author John Taylor
 */
public class ImageDisplayActivity extends InjectableActivity {

    private static final String TAG = TelescopeTouchApp.getTag(ImageDisplayActivity.class);
    private static final int ERROR_MAGIC_NUMBER = -1;
    private GalleryImage selectedImage;
    private ActivityLightLevelManager activityLightLevelManager;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getApplicationComponent().inject(this);
        setContentView(R.layout.activity_gallery_image);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        activityLightLevelManager = new ActivityLightLevelManager(
                new ActivityLightLevelChanger(this, null),
                PreferenceManager.getDefaultSharedPreferences(this));
        Intent intent = getIntent();
        Log.d(TAG, intent.toString());
        int position = intent.getIntExtra(ImageGalleryActivity.IMAGE_ID, ERROR_MAGIC_NUMBER);
        if (position == ERROR_MAGIC_NUMBER) {
            Log.e(TAG, "No position was provided with the intent - aborting.");
            finish();
        }

        selectedImage = GalleryFactory.getGallery(getResources()).getGalleryImages().get(position);
        this.<ImageView>findViewById(R.id.gallery_image).setImageResource(selectedImage.imageId);
        this.<TextView>findViewById(R.id.gallery_image_title).setText(selectedImage.name);
        this.<Button>findViewById(R.id.gallery_image_search_btn).setOnClickListener(source -> {
            Log.d(TAG, "Do Search");
            // We must ensure that all the relevant layers are actually visible or the search might
            // fail.  This is rather hacky.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Editor editor = sharedPreferences.edit();
            String[] keys = {"source_provider.0",  // Stars
                    "source_provider.2",  // Messier
                    "source_provider.3"};  // Planets
            for (String key : keys) {
                if (!sharedPreferences.getBoolean(key, false)) {
                    editor.putBoolean(key, true);
                }
            }
            editor.apply();
            Intent queryIntent = new Intent(this, DynamicStarMapActivity.class);
            queryIntent.setAction(Intent.ACTION_SEARCH);
            queryIntent.putExtra(SearchManager.QUERY, selectedImage.searchTerm);
            startActivity(queryIntent);
        });
        Button gotoButton = this.findViewById(R.id.gallery_point_telescope);
        String gotoName = selectedImage.getGotoName();
        gotoButton.setEnabled(!gotoName.equals("?"));
        gotoButton.setOnClickListener(source -> {
            Intent gotoIntent = new Intent(this, MainActivity.class);
            if (TelescopeTouchApp.connectionManager.isConnected()) {
                GoToFragment.setRequestedSearch(gotoName);
                gotoIntent.putExtra(MainActivity.ACTION, MainActivity.ACTION_SEARCH);
            } else {
                gotoIntent.putExtra(MainActivity.ACTION, MainActivity.ACTION_CONNECT);
                gotoIntent.putExtra(MainActivity.MESSAGE, R.string.connect_telescope_first);
            }
            startActivity(gotoIntent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        activityLightLevelManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
}
