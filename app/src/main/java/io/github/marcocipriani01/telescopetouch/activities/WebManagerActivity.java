/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
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
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;

/**
 * @author marcocipriani01
 */
public class WebManagerActivity extends AppCompatActivity {

    public static final String INTENT_HOST = "intent_host";
    private DarkerModeManager darkerModeManager;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        darkerModeManager = new DarkerModeManager(this, null, preferences);
        setTheme(darkerModeManager.getPref() ? R.style.DarkerAppTheme : R.style.AppTheme);
        WebView webView = new WebView(this);
        setContentView(webView);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setSupportZoom(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView web, String url) {
                webView.loadUrl("javascript:document.body.style.margin=\"16px\"; void 0");
            }
        });
        //TODO(marcocipriani01): customize port
        webView.loadUrl(Objects.requireNonNull(getIntent().getStringExtra(INTENT_HOST)) + ":8624");
        if (preferences.getBoolean(ApplicationConstants.WEB_MANAGER_INFO_PREF, true)) {
            new AlertDialog.Builder(this).setIcon(R.drawable.internet)
                    .setTitle(R.string.indi_web_manager)
                    .setPositiveButton(android.R.string.ok, (dialog, which) ->
                            preferences.edit().putBoolean(ApplicationConstants.WEB_MANAGER_INFO_PREF, false).apply())
                    .setMessage(R.string.web_manager_info).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        darkerModeManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        darkerModeManager.stop();
    }
}