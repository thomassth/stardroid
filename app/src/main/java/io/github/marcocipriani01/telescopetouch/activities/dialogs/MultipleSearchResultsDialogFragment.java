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

package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.SkyMapActivity;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;
import io.github.marcocipriani01.telescopetouch.search.SearchResult;

/**
 * Created by johntaylor on 4/3/16.
 */
public class MultipleSearchResultsDialogFragment extends DialogFragment {

    private static final String TAG = TelescopeTouchApp.getTag(MultipleSearchResultsDialogFragment.class);
    @Inject
    SkyMapActivity parentActivity;

    private ArrayAdapter<SearchResult> multipleSearchResultsAdaptor;

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) requireActivity()).getComponent().inject(this);

        // TODO(jontayler): inject
        multipleSearchResultsAdaptor = new ArrayAdapter<>(parentActivity, android.R.layout.simple_list_item_1, new ArrayList<>());

        DialogInterface.OnClickListener onClickListener = (dialog, whichButton) -> {
            if (whichButton == Dialog.BUTTON_NEGATIVE) {
                Log.d(TAG, "Many search results Dialog closed with cancel");
            } else {
                final SearchResult item = multipleSearchResultsAdaptor.getItem(whichButton);
                parentActivity.activateSearchTarget(item.coords, item.capitalizedName);
            }
            dialog.dismiss();
        };
        return new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.many_search_results_title)
                .setNegativeButton(android.R.string.cancel, onClickListener)
                .setAdapter(multipleSearchResultsAdaptor, onClickListener)
                .create();
    }

    public void clearResults() {
        multipleSearchResultsAdaptor.clear();
    }

    public void add(SearchResult result) {
        multipleSearchResultsAdaptor.add(result);
    }

    public interface ActivityComponent {
        void inject(MultipleSearchResultsDialogFragment fragment);
    }
}