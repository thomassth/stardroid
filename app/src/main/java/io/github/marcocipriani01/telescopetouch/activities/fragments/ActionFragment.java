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

    public boolean isActionEnabled() {
        return false;
    }

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

    public int getActionDrawable() {
        return 0;
    }

    @Override
    public void run() {

    }

    public interface ActionListener {

        void onActionDrawableChange(int resource);

        void setActionEnabled(boolean actionEnabled);

        void onActionSnackRequested(String msg);

        void onActionSnackRequested(@StringRes int msgRes, int actionName, View.OnClickListener action);

        void showActionbar();

        void onPermissionRequested(String permission);
    }
}