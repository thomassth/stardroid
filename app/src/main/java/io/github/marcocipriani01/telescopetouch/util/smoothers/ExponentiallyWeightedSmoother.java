package io.github.marcocipriani01.telescopetouch.util.smoothers;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import io.github.marcocipriani01.telescopetouch.util.MathUtil;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Exponentially weighted smoothing, as suggested by Chris M.
 */
public class ExponentiallyWeightedSmoother extends SensorSmoother {
    private static final String TAG = MiscUtil.getTag(ExponentiallyWeightedSmoother.class);
    private final float alpha;
    private final int exponent;
    private final float[] last = new float[3];
    private final float[] current = new float[3];

    public ExponentiallyWeightedSmoother(SensorEventListener listener, float alpha, int exponent) {
        super(listener);
        Log.d(TAG, "ExponentionallyWeightedSmoother with alpha = " + alpha + " and exp = " + exponent);
        this.alpha = alpha;
        this.exponent = exponent;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for (int i = 0; i < 3; ++i) {
            last[i] = current[i];
            float diff = event.values[i] - last[i];
            float correction = diff * alpha;
            for (int j = 1; j < exponent; ++j) {
                correction *= MathUtil.abs(diff);
            }
            if (correction > MathUtil.abs(diff) ||
                    correction < -MathUtil.abs(diff)) correction = diff;
            current[i] = last[i] + correction;
        }
        listener.onSensorChanged(event);
    }
}
