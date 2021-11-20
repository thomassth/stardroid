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
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.DynamicStarMapActivity
import com.google.android.stardroid.activities.ImageDisplayActivity
import com.google.android.stardroid.activities.util.ActivityLightLevelChanger
import com.google.android.stardroid.activities.util.ActivityLightLevelManager
import com.google.android.stardroid.gallery.GalleryFactory
import com.google.android.stardroid.gallery.GalleryImage
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.MiscUtil.getTag
import javax.inject.Inject

/**
 * Shows an image to the user and allows them to search for it.
 *
 * @author John Taylor
 */
class ImageDisplayActivity : InjectableActivity() {
    private var selectedImage: GalleryImage? = null
    private var activityLightLevelManager: ActivityLightLevelManager? = null

    @JvmField
    @Inject
    var analytics: Analytics? = null
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        applicationComponent!!.inject(this)
        setContentView(R.layout.imagedisplay)
        activityLightLevelManager = ActivityLightLevelManager(
            ActivityLightLevelChanger(this, null),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        val intent = intent
        Log.d(TAG, intent.toString())
        val position = intent.getIntExtra(ImageGalleryActivity.IMAGE_ID, ERROR_MAGIC_NUMBER)
        if (position == ERROR_MAGIC_NUMBER) {
            Log.e(TAG, "No position was provided with the intent - aborting.")
            finish()
        }
        val galleryImages = GalleryFactory.getGallery(resources).galleryImages
        selectedImage = galleryImages[position]
        val imageView = findViewById<View>(R.id.gallery_image) as ImageView
        imageView.setImageResource(selectedImage!!.imageId)
        val label = findViewById<View>(R.id.gallery_image_title) as TextView
        label.text = selectedImage!!.name
        val backButton = findViewById<View>(R.id.gallery_image_back_btn) as Button
        backButton.setOnClickListener { source: View? -> goBack(source) }
        val searchButton = findViewById<View>(R.id.gallery_image_search_btn) as Button
        searchButton.setOnClickListener { source: View? -> doSearch(source) }
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onResume() {
        super.onResume()
        activityLightLevelManager!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        activityLightLevelManager!!.onPause()
    }

    fun doSearch(source: View?) {
        Log.d(TAG, "Do Search")
        // We must ensure that all the relevant layers are actually visible or the search might
        // fail.  This is rather hacky.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        val keys = arrayOf(
            "source_provider.0",  // Stars
            "source_provider.2",  // Messier
            "source_provider.3"
        ) // Planets
        for (key in keys) {
            if (!sharedPreferences.getBoolean(key, false)) {
                editor.putBoolean(key, true)
            }
        }
        editor.commit()
        val queryIntent = Intent()
        queryIntent.action = Intent.ACTION_SEARCH
        queryIntent.putExtra(SearchManager.QUERY, selectedImage!!.searchTerm)
        queryIntent.setClass(this@ImageDisplayActivity, DynamicStarMapActivity::class.java)
        startActivity(queryIntent)
    }

    fun goBack(source: View?) {
        Log.d(TAG, "Go back")
        finish()
    }

    companion object {
        private val TAG = getTag(ImageDisplayActivity::class.java)
        private const val ERROR_MAGIC_NUMBER = -1
    }
}