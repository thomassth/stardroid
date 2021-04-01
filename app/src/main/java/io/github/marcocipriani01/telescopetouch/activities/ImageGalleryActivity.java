/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
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