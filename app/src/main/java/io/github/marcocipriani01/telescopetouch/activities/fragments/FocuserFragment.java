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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.properties.INDIStandardElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.CounterHandler;
import io.github.marcocipriani01.telescopetouch.activities.util.LongPressHandler;
import io.github.marcocipriani01.telescopetouch.indi.ConnectionManager;
import io.github.marcocipriani01.telescopetouch.indi.INDICamera;
import io.github.marcocipriani01.telescopetouch.indi.INDIFocuser;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

/**
 * This fragment shows directional buttons to move a focuser.
 *
 * @author marcocipriani01
 */
public class FocuserFragment extends ActionFragment
        implements View.OnClickListener, TextWatcher, INDIFocuser.FocuserListener, ConnectionManager.ManagerListener {

    private static final String TAG = TelescopeTouchApp.getTag(MountControlFragment.class);
    private static INDIDevice selectedFocuserDev = null;
    private final List<INDICamera> focusers = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Button inButton = null;
    private Button outButton = null;
    private Button stepsPerClickUpBtn = null;
    private Button stepsPerClickDownBtn = null;
    private Button abortButton = null;
    private Button setAbsPosButton = null;
    private Button syncBtn = null;
    private EditText stepsPerClickField = null;
    private EditText positionField = null;
    private Slider speedSlider = null;
    private CounterHandler stepsHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_focuser, container, false);
        inButton = rootView.findViewById(R.id.focus_in);
        outButton = rootView.findViewById(R.id.focus_out);
        stepsPerClickUpBtn = rootView.findViewById(R.id.focuser_steps_click_more);
        stepsPerClickDownBtn = rootView.findViewById(R.id.focuser_steps_click_less);
        stepsPerClickField = rootView.findViewById(R.id.focuser_steps_click_field);
        abortButton = rootView.findViewById(R.id.focuser_abort);
        setAbsPosButton = rootView.findViewById(R.id.fok_abs_pos_button);
        syncBtn = rootView.findViewById(R.id.fok_sync_pos_button);
        positionField = rootView.findViewById(R.id.abs_pos_field);
        speedSlider = rootView.findViewById(R.id.focus_speed_slider);
        stepsHandler = new CounterHandler(stepsPerClickUpBtn, stepsPerClickDownBtn, 1, 1000000, 100, 10, 100, false) {
            @Override
            protected void onIncrement() {
                super.onIncrement();
                stepsPerClickField.setText(String.valueOf(getValue()));
            }

            @Override
            protected void onDecrement() {
                super.onDecrement();
                stepsPerClickField.setText(String.valueOf(getValue()));
            }
        };
        new LongPressHandler(outButton, inButton, 150) {
            @Override
            protected void onIncrement() {
                INDIFocuser focuser = getFocuser();
                if ((focuser != null) && focuser.canMoveRelative()) {
                    try {
                        focuser.moveOutward(stepsHandler.getValue());
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage(), e);
                        errorSnackbar(e);
                    }
                } else {
                    onFocuserFunctionsChange();
                }
            }

            @Override
            protected void onDecrement() {
                INDIFocuser focuser = getFocuser();
                if ((focuser != null) && focuser.canMoveRelative()) {
                    try {
                        focuser.moveInward(stepsHandler.getValue());
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage(), e);
                        errorSnackbar(e);
                    }
                } else {
                    onFocuserFunctionsChange();
                }
            }
        };
        abortButton.setOnClickListener(this);
        setAbsPosButton.setOnClickListener(this);
        syncBtn.setOnClickListener(this);
        stepsPerClickField.addTextChangedListener(this);
        speedSlider.setOnSeekBarChangeListener(this);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectionManager.addINDIListener(this);
        // Enumerate existing properties
        if (connectionManager.isConnected()) {
            List<INDIDevice> list = connectionManager.getIndiConnection().getDevicesAsList();
            for (INDIDevice device : list) {
                device.addINDIDeviceListener(this);
                List<INDIProperty<?>> properties = device.getPropertiesAsList();
                for (INDIProperty<?> property : properties) {
                    newProperty0(device, property);
                }
            }
            updateSpeedBar();
        } else {
            clearVars();
        }
        // Update UI
        enableUi();
        updateStepsText();
        updatePositionText();
    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.removeINDIListener(this);
    }

    private void updateSpeedBar() {
        handler.post(() -> {
            if ((speedSlider != null) && (speedElem != null)) {
                speedSlider.setOnSeekBarChangeListener(null);
                double step = speedElem.getStep(), min = speedElem.getMin(), max = speedElem.getMax();
                speedSlider.setMax((int) ((max - min) / step));
                speedSlider.setProgress((int) ((speedElem.getValue() - min) / step));
                speedSlider.setOnSeekBarChangeListener(this);
            }
        });
    }

    /**
     * Updates the speed text
     */
    private void updateStepsText() {
        handler.post(() -> {
            if (stepsPerClickField != null) {
                if (relPosElem == null) {
                    stepsPerClickField.setText(R.string.unavailable);
                } else {
                    int steps = (int) (double) relPosElem.getValue();
                    if (steps == 0) steps = 10;
                    stepsPerClickField.setText(String.valueOf(steps));
                    stepsHandler.setValue(steps);
                }
            }
        });
    }

    private void updatePositionText() {
        handler.post(() -> {
            if (positionField != null) {
                if (absPosElem == null) {
                    positionField.setText(R.string.unavailable);
                } else {
                    positionField.setText(String.valueOf((int) (double) absPosElem.getValue()));
                }
            }
        });
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            stepsHandler.setValue(Integer.parseInt(s.toString()));
        } catch (NumberFormatException ignored) {

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        try {
            if (id == R.id.focuser_abort) {
                if (abortElem != null) {
                    abortElem.setDesiredValue(Constants.SwitchStatus.ON);
                    connectionManager.updateProperties(abortProp);
                }
            } else if (id == R.id.fok_abs_pos_button) {
                if (absPosElem != null && positionField != null) {
                    try {
                        absPosElem.setDesiredValue(Double.parseDouble(positionField.getText().toString()));
                        connectionManager.updateProperties(absPosProp);
                    } catch (NumberFormatException e) {
                        requestActionSnack(R.string.invalid_abs_position);
                        updatePositionText();
                    }
                }
            } else if (id == R.id.fok_sync_pos_button) {
                if (syncPosElem != null && positionField != null) {
                    try {
                        syncPosElem.setDesiredValue(Double.parseDouble(positionField.getText().toString()));
                        connectionManager.updateProperties(syncPosProp);
                    } catch (NumberFormatException e) {
                        requestActionSnack(R.string.invalid_abs_position);
                        updatePositionText();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (speedElem != null) {
            try {
                double step = speedElem.getStep(), min = speedElem.getMin();
                speedElem.setDesiredValue(min + (progress * step));
                connectionManager.updateProperties(speedProp);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        clearVars();
        updateSpeedBar();
        updateStepsText();
        updatePositionText();
        enableUi();
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        if (newProperty0(device, property))
            enableUi();
    }

    private boolean newProperty0(INDIDevice device, INDIProperty<?> property) {
        String name = property.getName(), devName = device.getName();
        Log.i(TAG, "New Property (" + name + ") added to device " + devName
                + ", elements: " + Arrays.toString(property.getElementNames()));
        switch (name) {
            case "ABS_FOCUS_POSITION": {
                if ((absPosElem = (INDINumberElement) property.getElement(INDIStandardElement.FOCUS_ABSOLUTE_POSITION)) != null) {
                    property.addINDIPropertyListener(this);
                    absPosProp = (INDINumberProperty) property;
                    updatePositionText();
                }
                return true;
            }
            case "REL_FOCUS_POSITION": {
                if ((relPosElem = (INDINumberElement) property.getElement(INDIStandardElement.FOCUS_RELATIVE_POSITION)) != null) {
                    relPosProp = (INDINumberProperty) property;
                    stepsHandler.setMaxValue((int) relPosElem.getMax());
                    stepsHandler.setMinValue((int) relPosElem.getMin());
                    updateStepsText();
                }
                return true;
            }
            case "FOCUS_MOTION": {
                if (((inwardDirElem = (INDISwitchElement) property.getElement(INDIStandardElement.FOCUS_INWARD)) != null)
                        && ((outwardDirElem = (INDISwitchElement) property.getElement(INDIStandardElement.FOCUS_OUTWARD)) != null)) {
                    directionProp = (INDISwitchProperty) property;
                    handler.post(() -> {
                        if (focuserName != null)
                            focuserName.setText(devName);
                        if (toolbar != null)
                            toolbar.setTitle(devName);
                    });
                }
                return true;
            }
            case "FOCUS_ABORT_MOTION": {
                if ((abortElem = (INDISwitchElement) property.getElement(INDIStandardElement.ABORT)) != null) {
                    abortProp = (INDISwitchProperty) property;
                }
                return true;
            }
            case "FOCUS_SPEED": {
                if ((speedElem = (INDINumberElement) property.getElement(INDIStandardElement.FOCUS_SPEED_VALUE)) != null) {
                    speedProp = (INDINumberProperty) property;
                    property.addINDIPropertyListener(this);
                    updateSpeedBar();
                }
                return true;
            }
            case "FOCUS_SYNC": {
                if ((syncPosElem = (INDINumberElement) property.getElement(INDIStandardElement.FOCUS_SYNC_VALUE)) != null) {
                    syncPosProp = (INDINumberProperty) property;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        String name = property.getName();
        Log.d(TAG, "Removed property (" + name + ") to device " + device.getName());
        switch (name) {
            case "REL_FOCUS_POSITION": {
                relPosElem = null;
                relPosProp = null;
                updateStepsText();
                break;
            }
            case "FOCUS_MOTION": {
                inwardDirElem = null;
                outwardDirElem = null;
                directionProp = null;
                handler.post(() -> {
                    if (focuserName != null)
                        focuserName.setText(R.string.focuser_control);
                    if (toolbar != null)
                        toolbar.setTitle(R.string.focuser_control);
                });
                break;
            }
            case "FOCUS_ABORT_MOTION": {
                abortElem = null;
                abortProp = null;
                break;
            }
            case "ABS_FOCUS_POSITION": {
                absPosProp = null;
                absPosElem = null;
                updatePositionText();
                break;
            }
            case "FOCUS_SPEED": {
                speedProp = null;
                speedElem = null;
                break;
            }
            case "FOCUS_SYNC": {
                syncPosProp = null;
                syncPosElem = null;
                break;
            }
            default: {
                return;
            }
        }
        enableUi();
    }

    @Override
    public boolean isActionEnabled() {
        return false;
    }

    @Override
    public int getActionDrawable() {
        return 0;
    }

    @Override
    public void run() {

    }

    private INDIFocuser getFocuser() {
        if (selectedFocuserDev == null) return null;
        return connectionManager.indiFocusers.get(selectedFocuserDev);
    }

    @Override
    public void onFocusersListChange() {

    }

    @Override
    public void onConnectionLost() {

    }

    public void errorSnackbar(Throwable e) {
        String message = e.getLocalizedMessage();
        if ((message == null) || message.equals("?")) {
            requestActionSnack(context.getString(R.string.unknown_exception));
        } else {
            requestActionSnack(context.getString(R.string.error) + " " + message);
        }
    }

    private void disableControls() {
        //TODO
    }

    @Override
    public void onFocuserFunctionsChange() {
        INDIFocuser focuser = getFocuser();
        if (focuser == null) {
            disableControls();
            return;
        }
        boolean canMoveRelative = focuser.canMoveRelative();
        if (inButton != null) inButton.setEnabled(canMoveRelative);
        if (outButton != null) outButton.setEnabled(canMoveRelative);
        if (stepsPerClickField != null) {
            stepsPerClickField.setEnabled(canMoveRelative);
            if (canMoveRelative) {
                int steps = (int) (double) focuser.relPositionE.getValue();
                if (steps == 0) steps = 10;
                stepsPerClickField.setText(String.valueOf(steps));
                stepsHandler.setValue(steps);
            } else {
                stepsPerClickField.setText(R.string.unavailable);
            }
        }
        if (stepsPerClickUpBtn != null) stepsPerClickUpBtn.setEnabled(canMoveRelative);
        if (stepsPerClickDownBtn != null) stepsPerClickDownBtn.setEnabled(canMoveRelative);
        if (abortButton != null) abortButton.setEnabled(focuser.canAbort());
        boolean canSync = focuser.canSync();
        if (syncBtn != null) syncBtn.setEnabled(canSync);
        boolean hasAbsolutePosition = focuser.hasAbsolutePosition();
        if (setAbsPosButton != null) setAbsPosButton.setEnabled(hasAbsolutePosition);
        if (positionField != null) positionField.setEnabled(hasAbsolutePosition || canSync);
        if (speedSlider != null) speedSlider.setEnabled(focuser.hasSpeed());
    }

    @Override
    public void onFocuserPositionChange(int position) {
        if (positionField != null) positionField.setText(String.valueOf(position));
    }

    @Override
    public void onFocuserSpeedChange(int value) {
        if (speedSlider != null) speedSlider.setValue(value);
    }
}