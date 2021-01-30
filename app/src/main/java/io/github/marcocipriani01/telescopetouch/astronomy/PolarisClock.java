package io.github.marcocipriani01.telescopetouch.astronomy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.TimeZone;

public class PolarisClock {

    private final PolarisPrecession northPolarisPrecession = new PolarisPrecession(true);
    private final PolarisPrecession southPolarisPrecession = new PolarisPrecession(false);
    private Context context;
    private int day;
    private double deltaJulianDateTime;
    private long deltaJulianDay;
    private int dstOffset = Calendar.getInstance().get(16);
    private final int utcOffsetMs = (Calendar.getInstance().get(15) + this.dstOffset);
    private int hour;
    private int hourUTC;
    private boolean isLocationValid = false;
    private long julianDay;
    private double latitude;
    private double longitude;
    private int minute;
    private int minuteUTC;
    private int month;
    private boolean northCurrentHemisphere;
    private double polarisClock;
    private double polarisDEC;
    private double polarisInScope;
    private double polarisRA;
    private int second;
    private int secondUTC;
    private double siderealTime;
    private int year;

    PolarisClock(Context context2) {
        this.context = context2;
        refreshTime();
    }

    @SuppressLint("DefaultLocale")
    public String getDateDSTString() {
        String str;
        String string = PreferenceManager.getDefaultSharedPreferences(this.context).getString("date_format_selection", "");
        if (string.equals("monthdayyear")) {
            str = String.format("%1$02d/%2$02d/%3$02d", this.month, this.day, this.year);
        } else if (string.equals("daymonthyear")) {
            str = String.format("%1$02d/%2$02d/%3$02d", this.day, this.month, this.year);
        } else {
            str = String.format("%1$02d/%2$02d/%3$02d", this.year, this.month, this.day);
        }
        if (this.dstOffset == 0) {
            return str + "   NO DST";
        }
        return str + "   DST " + Integer.toString(this.dstOffset / 3600000) + "h";
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

    public int getPolarisRotation() {
        return (int) this.polarisInScope;
    }

    public String getPolarClockInScopeString() {
        if (this.northCurrentHemisphere) {
            this.polarisInScope = ((360.0d - this.polarisClock) + 180.0d) % 360.0d;
        } else {
            this.polarisInScope = (this.polarisClock + 180.0d) % 360.0d;
        }
        return getStringOfDegree(this.polarisInScope / 30.0d, false);
    }

    public String getSiderealClockTimeString() {
        if (this.isLocationValid) {
            return getStringOfDegree(this.siderealTime / 15.0d, false);
        }
        return getStringOfDegree(0.0d, false);
    }

    public String getSiderealClockDegString() {
        if (this.isLocationValid) {
            return getStringOfDegree(this.siderealTime, true);
        }
        return getStringOfDegree(0.0d, true);
    }

    @SuppressLint("DefaultLocale")
    public String getLocalClockTimeString() {
        return String.format("%1$02d:%2$02d:%3$02d", this.hour, this.minute, this.second);
    }

    public String getLocalClockDegString() {
        return getStringOfDegree(((double) (this.hour * 15)) + (((double) this.minute) / 4.0d) + (((double) this.second) / 240.0d), true);
    }

    @SuppressLint("DefaultLocale")
    public String getUTCClockTimeString() {
        return String.format("%1$02d:%2$02d:%3$02d", this.hourUTC, this.minuteUTC, this.secondUTC);
    }

    public String getUTCClockDegString() {
        return getStringOfDegree(((double) (this.hourUTC * 15)) + (((double) this.minuteUTC) / 4.0d) + (((double) this.secondUTC) / 240.0d), true);
    }

    public String getPolarisPrecessedCoordsString() {
        return getPolarisPrecessedRAString() + "/" + getPolarisPrecessedDECString();
    }

    public String getPolarisOriginCoordsString() {
        return this.northCurrentHemisphere ? "02h31m49s/89°15'50\"" : "21h08m46s/-88°57'23\"";
    }

    public String getStarNameString() {
        return this.northCurrentHemisphere ? "Polaris data" : "Sig Oct data";
    }

    public void refreshTimeGPSInformation() {
        refreshTime();
        generatePolarClock();
    }

    private String getPolarisPrecessedRAString() {
        if (this.isLocationValid) {
            return getStringOfDegree(this.polarisRA / 15.0d, false);
        }
        return getStringOfDegree(0.0d, false);
    }

    private String getPolarisPrecessedDECString() {
        if (this.isLocationValid) {
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
        if (this.isLocationValid) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            boolean automaticHemisphereDetection = defaultSharedPreferences.getBoolean("hemisphere_autodetect", true);
            boolean z = automaticHemisphereDetection || !defaultSharedPreferences.getString("manual_hemisphere_selection", "").equals("south");
            this.polarisRA = this.northPolarisPrecession.getPrecessedRA();
            this.polarisDEC = this.northPolarisPrecession.getPrecessedDEC();
            this.northCurrentHemisphere = true;
            if ((!automaticHemisphereDetection && !z) || (automaticHemisphereDetection && this.latitude < 0.0d)) {
                this.polarisRA = this.southPolarisPrecession.getPrecessedRA();
                this.polarisDEC = this.southPolarisPrecession.getPrecessedDEC();
                this.northCurrentHemisphere = false;
            }
            double d = this.siderealTime;
            double d2 = this.polarisRA;
            if (d > d2) {
                this.polarisClock = d - d2;
            } else {
                this.polarisClock = (d + 360.0d) - d2;
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
        Calendar instance = Calendar.getInstance();
        Calendar instance2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        this.year = instance.get(1);
        this.month = instance.get(2) + 1;
        this.day = instance.get(5);
        this.hour = instance.get(11);
        this.minute = instance.get(12);
        this.second = instance.get(13);
        this.hourUTC = instance2.get(11);
        this.minuteUTC = instance2.get(12);
        this.secondUTC = instance2.get(13);
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
        this.julianDay = (long) ((((((this.day + (((((i + (i2 * 12)) - 3) * 153) + 2) / 5)) + (i3 * 365)) + (i3 / 4)) - (i3 / 100)) + (i3 / 400)) - 32045);
    }

    public void setLatitude(double d) {
        this.latitude = d;
    }

    public void setLongitude(double d) {
        this.longitude = d;
    }

    public void setLocationValid(boolean z) {
        this.isLocationValid = z;
        if (!z) {
            this.latitude = 0.0d;
            this.longitude = 0.0d;
        }
    }

    public boolean northCurrentHemisphere() {
        return this.northCurrentHemisphere;
    }
}