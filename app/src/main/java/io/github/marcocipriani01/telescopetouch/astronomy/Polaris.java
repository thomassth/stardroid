package io.github.marcocipriani01.telescopetouch.astronomy;

import android.annotation.SuppressLint;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.R;

import static io.github.marcocipriani01.telescopetouch.util.TimeUtils.meanSiderealTime;

public class Polaris {

    public static final double POLARIS_J2000_RA = 37.954560666666666;    // Might be 30.5303040444 (?)
    public static final double POLARIS_J2000_DEC = 89.26410897222;
    public static final double SIG_OCT_J2000_RA = 315.14634539559486;
    public static final double SIG_OCT_J2000_DEC = -88.956503248687222;
    private boolean locationValid = false;
    private boolean autoHemisphereDetection = true;
    private double latitude;
    private double longitude;
    private boolean northernHemisphere;
    private double hourAngle = 0.0;
    private double scopePosition;

    @SuppressLint("DefaultLocale")
    private static String angleToString(double angle, boolean degOrHourFormat) {
        int deg = (int) angle;
        double tmp = (angle % 1.0) * 3600.0;
        int min = Math.abs((int) (tmp / 60.0));
        int sec = Math.abs((int) (tmp % 60.0));
        if (degOrHourFormat) {
            return String.format("%1$02d°%2$02d'%3$02d\"", deg, min, sec);
        } else {
            return String.format("%1$02dh%2$02dm%3$02ds", deg, min, sec);
        }
    }

    public void setNorthernHemisphere(boolean northernHemisphere) {
        this.northernHemisphere = northernHemisphere;
    }

    public void setAutoHemisphereDetection(boolean autoHemisphereDetection) {
        this.autoHemisphereDetection = autoHemisphereDetection;
    }

    public String getHourAngleString() {
        return angleToString(this.hourAngle / 15.0d, false);
    }

    public String getLatitudeString() {
        return angleToString(Math.abs(this.latitude), true) + (this.latitude >= 0.0d ? " N" : " S");
    }

    public String getLongitudeString() {
        return angleToString(Math.abs(this.longitude), true) + (this.longitude >= 0.0 ? " E" : " W");
    }

    public float getScopePosition() {
        return (float) this.scopePosition;
    }

    public String getScopePositionString() {
        return angleToString(this.scopePosition / 30.0, false);
    }

    public int getStarName() {
        return this.northernHemisphere ? R.string.polaris_star_finder : R.string.sigma_octantis;
    }

    public void refresh() {
        if (this.locationValid) {
            if (this.autoHemisphereDetection)
                northernHemisphere = (this.latitude >= 0.0);
            Calendar calendar = Calendar.getInstance();
            double[] precession;
            if (northernHemisphere) {
                precession = StarsPrecession.precess(calendar, POLARIS_J2000_RA, POLARIS_J2000_DEC);
            } else {
                precession = StarsPrecession.precess(calendar, SIG_OCT_J2000_RA, SIG_OCT_J2000_DEC);
            }
            double rightAscension = precession[0];
            double siderealTime = meanSiderealTime(calendar, (float) longitude);
            if (siderealTime > rightAscension) {
                this.hourAngle = siderealTime - rightAscension;
            } else {
                this.hourAngle = (siderealTime + 360.0d) - rightAscension;
            }
            if (northernHemisphere) {
                this.scopePosition = ((360.0 - this.hourAngle) + 180.0) % 360.0;
            } else {
                this.scopePosition = (this.hourAngle + 180.0) % 360.0;
            }
        } else {
            this.hourAngle = 0.0d;
        }
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        locationValid = true;
        refresh();
    }
}