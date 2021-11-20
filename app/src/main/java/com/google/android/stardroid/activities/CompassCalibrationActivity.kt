package com.google.android.stardroid.activities

import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.CompassCalibrationActivity
import com.google.android.stardroid.activities.util.SensorAccuracyDecoder
import com.google.android.stardroid.util.Analytics
import com.google.android.stardroid.util.MiscUtil.getTag
import com.google.android.stardroid.util.Toaster
import javax.inject.Inject

class CompassCalibrationActivity : InjectableActivity(), SensorEventListener {
    private var magneticSensor: Sensor? = null
    private var checkBoxView: CheckBox? = null

    @JvmField
    @Inject
    var sensorManager: SensorManager? = null

    @JvmField
    @Inject
    var accuracyDecoder: SensorAccuracyDecoder? = null

    @JvmField
    @Inject
    var sharedPreferences: SharedPreferences? = null

    @JvmField
    @Inject
    var analytics: Analytics? = null

    @JvmField
    @Inject
    var toaster: Toaster? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerCompassCalibrationComponent.builder()
            .applicationComponent(applicationComponent)
            .compassCalibrationModule(CompassCalibrationModule(this)).build().inject(this)
        setContentView(R.layout.activity_compass_calibration)
        val web = findViewById<View>(R.id.compass_calib_activity_webview) as WebView
        web.loadUrl("file:///android_asset/html/how_to_calibrate.html")
        checkBoxView = findViewById<View>(R.id.compass_calib_activity_donotshow) as CheckBox
        val hideCheckbox = intent.getBooleanExtra(HIDE_CHECKBOX, false)
        if (hideCheckbox) {
            checkBoxView!!.visibility = View.GONE
            val reasonText = findViewById<View>(R.id.compass_calib_activity_explain_why)
            reasonText.visibility = View.GONE
        }
        magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticSensor == null) {
            (findViewById<View>(R.id.compass_calib_activity_compass_accuracy) as TextView).text =
                getString(R.string.sensor_absent)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (magneticSensor != null) {
            sensorManager!!.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    public override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
        if (checkBoxView!!.isChecked) {
            sharedPreferences!!.edit().putBoolean(DONT_SHOW_CALIBRATION_DIALOG, true).commit()
        }
    }

    private var accuracyReceived = false
    override fun onSensorChanged(event: SensorEvent) {
        if (!accuracyReceived) {
            onAccuracyChanged(event.sensor, event.accuracy)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        accuracyReceived = true
        val accuracyTextView = findViewById<TextView>(R.id.compass_calib_activity_compass_accuracy)
        val accuracyText = accuracyDecoder!!.getTextForAccuracy(accuracy)
        accuracyTextView.text = accuracyText
        accuracyTextView.setTextColor(accuracyDecoder!!.getColorForAccuracy(accuracy))
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH
            && intent.getBooleanExtra(AUTO_DISMISSABLE, false)
        ) {
            toaster!!.toastLong(R.string.sensor_accuracy_high)
            finish()
        }
    }

    fun onOkClicked(unused: View?) {
        finish()
    }

    public override fun onStart() {
        super.onStart()
    }

    companion object {
        const val HIDE_CHECKBOX = "hide checkbox"
        const val DONT_SHOW_CALIBRATION_DIALOG = "no calibration dialog"
        const val AUTO_DISMISSABLE = "auto dismissable"
        private val TAG = getTag(CompassCalibrationActivity::class.java)
    }
}