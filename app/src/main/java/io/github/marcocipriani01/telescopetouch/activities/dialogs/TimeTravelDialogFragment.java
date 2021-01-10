package io.github.marcocipriani01.telescopetouch.activities.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;
import io.github.marcocipriani01.telescopetouch.activities.DynamicStarMapActivity;
import io.github.marcocipriani01.telescopetouch.ephemeris.Planet;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;
import io.github.marcocipriani01.telescopetouch.util.TimeUtil;

/**
 * Time travel dialog fragment.
 */
public class TimeTravelDialogFragment extends DialogFragment {

    private static final String TAG = TelescopeTouchApplication.getTag(TimeTravelDialogFragment.class);
    private static final int MIN_CLICK_TIME = 1000;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
    // This is the date we will apply to the controller when the user hits go.
    private final Calendar calendar = Calendar.getInstance();
    @Inject
    DynamicStarMapActivity parentActivity;
    private Spinner popularDatesMenu;
    private TextView dateTimeReadout;
    private long lastClickTime = 0;

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Activities using this dialog MUST implement this interface.  Obviously.
        ((HasComponent<ActivityComponent>) requireActivity()).getComponent().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        View root = View.inflate(parentActivity, R.layout.time_dialog, null);
        builder.setView(root);
        builder.setTitle(R.string.menu_time);
        builder.setPositiveButton(R.string.go, (dialog, which) -> parentActivity.setTimeTravelMode(calendar.getTime()));
        builder.setNegativeButton(R.string.cancel, null);
        dateTimeReadout = root.findViewById(R.id.dateDisplay);

        root.findViewById(R.id.pickDate).setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_TIME) return;
            lastClickTime = SystemClock.elapsedRealtime();
            new DatePickerDialog(TimeTravelDialogFragment.this.getContext(),
                    (view, year, monthOfYear, dayOfMonth) -> {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        updateDisplay();
                        Log.d(TAG, "Setting date to: " + year + "-" + monthOfYear + "-" + dayOfMonth);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)) {
            }.show();
        });

        root.<Button>findViewById(R.id.pickTime).setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_TIME) return;
            lastClickTime = SystemClock.elapsedRealtime();
            ((Dialog) new TimePickerDialog(TimeTravelDialogFragment.this.getContext(),
                    (view, hour, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        updateDisplay();
                        Log.d(TAG, "Setting time to: " + hour + ":" + minute);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true) {
            }).show();
        });

        popularDatesMenu = root.findViewById(R.id.popular_dates_spinner);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                this.getContext(), R.array.popular_date_examples, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        popularDatesMenu.setAdapter(adapter);
        popularDatesMenu.setSelection(1);
        popularDatesMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // The callback received when the user selects a menu item.
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String s = (String) popularDatesMenu.getSelectedItem();
                Log.d(TAG, "Popular date " + popularDatesMenu.getSelectedItemPosition() + "  " + s);
                switch (popularDatesMenu.getSelectedItemPosition()) {
                    case 0:  // Now
                        calendar.setTime(new Date());
                        break;
                    case 1:  // Next sunset
                        setToNextSunRiseOrSet(Planet.RiseSetIndicator.SET);
                        break;
                    case 2:  // Next sunrise
                        setToNextSunRiseOrSet(Planet.RiseSetIndicator.RISE);
                        break;
                    case 3:  // Next full moon
                        Calendar nextFullMoon = Planet.getNextFullMoon(calendar);
                        setDate(nextFullMoon);
                        break;
                    case 4:  // Moon Landing 1969.
                        setDate(new GregorianCalendar(1969, GregorianCalendar.JULY, 20, 20, 27, 39));
                        break;
                    default:
                        Log.d(TAG, "Incorrect popular date index!");
                }
                updateDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Do nothing in this case.
            }
        });
        // Start by initializing ourselves to 'now'.  Note that this is the value
        // the first time the dialog is shown.  Thereafter it will remember the
        // last value set.
        calendar.setTime(new Date());
        updateDisplay();

        return builder.create();
    }

    /**
     * Sets the internal calendar of this dialog to the given date.
     */
    private void setDate(Calendar date) {
        calendar.setTimeInMillis(date.getTimeInMillis());
        updateDisplay();
    }

    private void updateDisplay() {
        dateTimeReadout.setText(parentActivity.getString(R.string.now_visiting,
                dateFormat.format(calendar.getTime())));
    }

    private void setToNextSunRiseOrSet(Planet.RiseSetIndicator indicator) {
        Calendar riseSet = Planet.Sun.calcNextRiseSetTime(calendar, parentActivity.getModel().getLocation(), indicator);
        if (riseSet == null) {
            Toast.makeText(this.getContext(), R.string.sun_wont_set_message, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Sun rise or set is at: " + TimeUtil.normalizeHours(
                    riseSet.get(Calendar.HOUR_OF_DAY)) + ":" + riseSet.get(Calendar.MINUTE));
            setDate(riseSet);
        }
    }

    public interface ActivityComponent {
        void inject(TimeTravelDialogFragment fragment);
    }
}