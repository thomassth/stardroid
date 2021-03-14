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

package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

public abstract class ActionFragment extends Fragment implements Runnable {

    protected Context context;
    private volatile ActionListener listener = null;

    public void setActionEnabledListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public abstract boolean isActionEnabled();

    public void notifyActionChange() {
        if (listener != null) listener.setActionEnabled(isActionEnabled());
    }

    public void notifyActionDrawableChange() {
        if (listener != null) listener.onActionDrawableChange(getActionDrawable());
    }

    public void requestActionSnack(@StringRes int msgRes) {
        if (listener != null) listener.onActionSnackRequested(context.getString(msgRes));
    }

    public void requestActionSnack(String msg) {
        if (listener != null) listener.onActionSnackRequested(msg);
    }

    @SuppressWarnings("SameParameterValue")
    public void requestActionSnack(@StringRes int msgRes, int actionName, View.OnClickListener action) {
        if (listener != null) listener.onActionSnackRequested(msgRes, actionName, action);
    }

    public void showActionbar() {
        if (listener != null) listener.showActionbar();
    }

    public void requestPermission(String permission) {
        if (listener != null) listener.onPermissionRequested(permission);
    }

    public void onPermissionAcquired(String permission) {

    }

    public void onPermissionNotAcquired(String permission) {

    }

    public abstract int getActionDrawable();

    public interface ActionListener {

        void onActionDrawableChange(int resource);

        void setActionEnabled(boolean actionEnabled);

        void onActionSnackRequested(String msg);

        void onActionSnackRequested(@StringRes int msgRes, int actionName, View.OnClickListener action);

        void showActionbar();

        void onPermissionRequested(String permission);
    }
}