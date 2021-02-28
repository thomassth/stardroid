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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;
import io.github.marcocipriani01.telescopetouch.gallery.GalleryImages;

/**
 * Displays a series of images to the user.  Selecting an image
 * invokes Sky Map Search.
 *
 * @author John Taylor
 */
public class ImageGalleryActivity extends AppCompatActivity {

    /**
     * The index of the image id Intent extra.
     */
    public static final String IMAGE_ID = "image_id";
    private DarkerModeManager darkerModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TelescopeTouchApp) getApplication()).getApplicationComponent().inject(this);
        darkerModeManager = new DarkerModeManager(this, null, PreferenceManager.getDefaultSharedPreferences(this));
        setTheme(darkerModeManager.getPref() ? R.style.DarkerAppTheme : R.style.AppTheme);
        setContentView(R.layout.activity_gallery);
        RecyclerView recyclerView = findViewById(R.id.gallery_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ImageAdapter());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        darkerModeManager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        darkerModeManager.stop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, final int position) {
            GalleryImages galleryImages = GalleryImages.values()[position];
            holder.galleryImage.setImageResource(galleryImages.getImageId());
            holder.galleryTitle.setText(galleryImages.getName(ImageGalleryActivity.this));
            holder.galleryItemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ImageGalleryActivity.this, ImageDisplayActivity.class);
                intent.putExtra(ImageGalleryActivity.IMAGE_ID, position);
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(
                        ImageGalleryActivity.this, holder.galleryImage, "ImageDisplayActivity").toBundle());
            });
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return GalleryImages.values().length;
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView galleryImage;
            TextView galleryTitle;
            LinearLayout galleryItemLayout;

            ImageViewHolder(View v) {
                super(v);
                this.galleryImage = v.findViewById(R.id.image_gallery_image);
                this.galleryTitle = v.findViewById(R.id.image_gallery_title);
                this.galleryItemLayout = v.findViewById(R.id.galleryItemLayout);
            }
        }
    }
}