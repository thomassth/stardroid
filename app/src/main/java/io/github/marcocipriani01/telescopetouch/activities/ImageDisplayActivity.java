package io.github.marcocipriani01.telescopetouch.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import java.util.List;

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
        setContentView(R.layout.imagedisplay);
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

        List<GalleryImage> galleryImages = GalleryFactory.getGallery(getResources()).getGalleryImages();
        selectedImage = galleryImages.get(position);
        ImageView imageView = findViewById(R.id.gallery_image);
        imageView.setImageResource(selectedImage.imageId);
        TextView label = findViewById(R.id.gallery_image_title);
        label.setText(selectedImage.name);
        Button backButton = findViewById(R.id.gallery_image_back_btn);
        backButton.setOnClickListener(this::goBack);
        Button searchButton = findViewById(R.id.gallery_image_search_btn);
        searchButton.setOnClickListener(this::doSearch);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
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

    public void doSearch(View source) {
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

        Intent queryIntent = new Intent();
        queryIntent.setAction(Intent.ACTION_SEARCH);
        queryIntent.putExtra(SearchManager.QUERY, selectedImage.searchTerm);
        queryIntent.setClass(ImageDisplayActivity.this, DynamicStarMapActivity.class);
        startActivity(queryIntent);
    }

    public void goBack(View source) {
        Log.d(TAG, "Go back");
        finish();
    }
}
