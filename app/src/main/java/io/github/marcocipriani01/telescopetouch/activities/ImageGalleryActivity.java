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
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelChanger;
import io.github.marcocipriani01.telescopetouch.activities.util.ActivityLightLevelManager;
import io.github.marcocipriani01.telescopetouch.gallery.GalleryFactory;
import io.github.marcocipriani01.telescopetouch.gallery.GalleryImage;

/**
 * Displays a series of images to the user.  Selecting an image
 * invokes Sky Map Search.
 *
 * @author John Taylor
 */
public class ImageGalleryActivity extends InjectableActivity {

    /**
     * The index of the image id Intent extra.
     */
    public static final String IMAGE_ID = "image_id";

    private List<GalleryImage> galleryImages;
    private ActivityLightLevelManager activityLightLevelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
        setContentView(R.layout.activity_gallery);
        activityLightLevelManager = new ActivityLightLevelManager(
                new ActivityLightLevelChanger(this, null),
                PreferenceManager.getDefaultSharedPreferences(this));
        this.galleryImages = GalleryFactory.getGallery(getResources()).getGalleryImages();
        addImagesToGallery();

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

    private void addImagesToGallery() {
        RecyclerView mRecyclerView = findViewById(R.id.gallery_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ImageAdapter imageAdapter = new ImageAdapter();
        mRecyclerView.setAdapter(imageAdapter);
    }

    /**
     * Starts the display image activity, and overrides the transition animation.
     */
    private void showImage(int position) {
        Intent intent = new Intent(ImageGalleryActivity.this, ImageDisplayActivity.class);
        intent.putExtra(ImageGalleryActivity.IMAGE_ID, position);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fastzoom);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

        @NonNull
        @Override
        public ImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageAdapter.MyViewHolder holder, final int position) {
            holder.galleryImage.setImageResource(galleryImages.get(position).imageId);
            holder.galleryTitle.setText(galleryImages.get(position).name);
            holder.galleryItemLayout.setOnClickListener(v -> showImage(position));
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return galleryImages.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView galleryImage;
            TextView galleryTitle;
            LinearLayout galleryItemLayout;

            MyViewHolder(View v) {
                super(v);
                this.galleryImage = v.findViewById(R.id.image_gallery_image);
                this.galleryTitle = v.findViewById(R.id.image_gallery_title);
                this.galleryItemLayout = v.findViewById(R.id.galleryItemLayout);
            }
        }
    }
}