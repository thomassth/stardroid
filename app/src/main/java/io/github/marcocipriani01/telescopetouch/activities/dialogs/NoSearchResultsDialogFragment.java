/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01) and the Sky Map Team
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

package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;

/**
 * End User License agreement dialog.
 * Created by johntaylor on 4/3/16.
 */
public class NoSearchResultsDialogFragment extends DialogFragment {

    private static final String TAG = TelescopeTouchApp.getTag(NoSearchResultsDialogFragment.class);
    @Inject
    Activity parentActivity;

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) requireActivity()).getComponent().inject(this);

        return new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.no_search_title).setMessage(R.string.no_search_results_text)
                .setNegativeButton(android.R.string.ok, null).create();
    }

    public interface ActivityComponent {
        void inject(NoSearchResultsDialogFragment fragment);
    }
}