package io.github.marcocipriani01.telescopetouch.astronomy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

public class Polaris {

    private final StarsPrecession northPolarisPrecession = new StarsPrecession(true);
    private final StarsPrecession southPolarisPrecession = new StarsPrecession(false);
    private final Context context;
    private final int utcOffsetMs;
    private int day;
    private double deltaJulianDateTime;
    private long deltaJulianDay;
    private int hour;
    private boolean locationValid = false;
    private long julianDay;
    private double latitude;
    private double longitude;
    private int minute;
    private int month;
    private boolean northernHemisphere;
    private double polarisClock;
    private double polarisDEC;
    private double polarisInScope;
    private double polarisRA;
    private int second;
    private double siderealTime;
    private int year;

    public Polaris(Context context) {
        this.context = context;
        Calendar calendar = Calendar.getInstance();
        utcOffsetMs = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET));
        refreshTime();
    }

    public String getPolarClockTimeString() {
        return getStringOfDegree(this.polarisClock / 15.0d, false);
    }

    public String getPolarClockDegSting() {
        return getStringOfDegree(this.polarisClock, true);
    }

    public String getLatitudeString() {
        String str = this.latitude >= 0.0d ? "N " : "S ";
        return str + getStringOfDegree(Math.abs(this.latitude), true);
    }

    public String getLongitudeString() {
        String str = this.longitude >= 0.0d ? "E " : "W ";
        return str + getStringOfDegree(Math.abs(this.longitude), true);
    }

    public double getPolarisRotation() {
        return this.polarisInScope;
    }

    public String getPolarClockInScopeString() {
        if (this.northernHemisphere) {
            this.polarisInScope = ((360.0d - this.polarisClock) + 180.0d) % 360.0d;
        } else {
            this.polarisInScope = (this.polarisClock + 180.0d) % 360.0d;
        }
        return getStringOfDegree(this.polarisInScope / 30.0d, false);
    }

    public String getSiderealClockTimeString() {
        if (this.locationValid) {
            return getStringOfDegree(this.siderealTime / 15.0d, false);
        }
        return getStringOfDegree(0.0d, false);
    }

    public String getSiderealClockDegString() {
        if (this.locationValid) {
            return getStringOfDegree(this.siderealTime, true);
        }
        return getStringOfDegree(0.0d, true);
    }

    public String getPolarisPrecessedCoordsString() {
        return getPolarisPrecessedRAString() + "/" + getPolarisPrecessedDECString();
    }

    public String getPolarisOriginCoordsString() {
        return this.northernHemisphere ? "02h31m49s/89°15'50\"" : "21h08m46s/-88°57'23\"";
    }

    public String getStarNameString() {
        return this.northernHemisphere ? "Polaris" : "Sig Oct";
    }

    private String getPolarisPrecessedRAString() {
        if (this.locationValid) {
            return getStringOfDegree(this.polarisRA / 15.0d, false);
        }
        return getStringOfDegree(0.0d, false);
    }

    private String getPolarisPrecessedDECString() {
        if (this.locationValid) {
            return getStringOfDegree(this.polarisDEC, true);
        }
        return getStringOfDegree(0.0d, true);
    }

    @SuppressLint("DefaultLocale")
    private String getStringOfDegree(double d, boolean z) {
        int i = (int) d;
        double d2 = (d % 1.0d) * 3600.0d;
        int abs = Math.abs((int) (d2 / 60.0d));
        int abs2 = Math.abs((int) (d2 % 60.0d));
        if (z) {
            return String.format("%1$02d°%2$02d'%3$02d''", i, abs, abs2);
        }
        return String.format("%1$02dh%2$02dm%3$02ds", i, abs, abs2);
    }

    private void generatePolarClock() {
        generateSiderealTime();
        if (this.locationValid) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            boolean automaticHemisphereDetection = defaultSharedPreferences.getBoolean("hemisphere_autodetect", true);
            boolean z = automaticHemisphereDetection || !defaultSharedPreferences.getString("manual_hemisphere_selection", "").equals("south");
            this.polarisRA = this.northPolarisPrecession.getPrecessedRA();
            this.polarisDEC = this.northPolarisPrecession.getPrecessedDec();
            this.northernHemisphere = true;
            if ((!automaticHemisphereDetection && !z) || (automaticHemisphereDetection && this.latitude < 0.0d)) {
                this.polarisRA = this.southPolarisPrecession.getPrecessedRA();
                this.polarisDEC = this.southPolarisPrecession.getPrecessedDec();
                this.northernHemisphere = false;
            }
            double siderealTime = this.siderealTime;
            if (siderealTime > this.polarisRA) {
                this.polarisClock = siderealTime - this.polarisRA;
            } else {
                this.polarisClock = (siderealTime + 360.0d) - this.polarisRA;
            }
        } else {
            this.polarisClock = 0.0d;
        }
    }

    private void generateSiderealTime() {
        double d = this.deltaJulianDateTime;
        double pow = (360.985647366286d * d) + 99.967794687d + (Math.pow(d, 2.0d) * 2.907879E-13d) + (Math.pow(this.deltaJulianDateTime, 3.0d) * -5.302E-22d) + this.longitude;
        this.siderealTime = pow;
        this.siderealTime = pow % 360.0d;
    }

    private void refreshTime() {
        Calendar calendar = Calendar.getInstance();
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DATE);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
        generateJulianDay();
        generateDeltaJulianDay();
        generateDeltaJulianDateTime();
    }

    private void generateDeltaJulianDateTime() {
        this.deltaJulianDateTime = ((double) this.deltaJulianDay) + ((((((double) this.hour) + (((double) this.minute) / 60.0d)) + (((double) this.second) / 3600.0d)) - ((double) (this.utcOffsetMs / 3600000))) / 24.0d);
    }

    private void generateDeltaJulianDay() {
            this.deltaJulianDay = (this.julianDay - 2451544) - 1;
    }

    private void generateJulianDay() {
        int i = this.month;
        int i2 = (14 - i) / 12;
        int i3 = (this.year + 4800) - i2;
        this.julianDay = (((((this.day + (((((i + (i2 * 12)) - 3) * 153) + 2) / 5)) + (i3 * 365)) + (i3 / 4)) - (i3 / 100)) + (i3 / 400)) - 32045;
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        locationValid = true;
        refreshTime();
        generatePolarClock();
    }

    public boolean northCurrentHemisphere() {
        return this.northernHemisphere;
    }
}