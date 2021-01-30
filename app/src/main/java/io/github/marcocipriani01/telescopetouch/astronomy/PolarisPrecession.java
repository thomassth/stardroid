package io.github.marcocipriani01.telescopetouch.astronomy;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.Calendar;

public class PolarisPrecession {

    private final double currentEpoch = (((double) Calendar.getInstance().get(1)) + (((double) Calendar.getInstance().get(6)) / 365.0d));
    private double polarisJ2000DEC = 89.26410897222222d;
    private double polarisJ2000RA = 37.954560666666666d;
    private double polarisPrecessedDEC;
    private double polarisPrecessedRA;

    public PolarisPrecession(boolean z) {
        if (!z) {
            this.polarisJ2000RA = 317.1951809166667d;
            this.polarisJ2000DEC = -88.95650324722223d;
        }
        calculateRigorousPrecession();
        Log.d("CommonDebug", "Calculated precession: " + this.polarisPrecessedRA / 15.0d);
        Log.d("CommonDebug", "J2000RA: " + this.polarisJ2000RA);
        Log.d("CommonDebug", "J2000DEC: " + this.polarisJ2000DEC);
        Log.d("CommonDebug", "Current epoch: " + this.currentEpoch);
    }

    private void calculatePrecession() {
        double d = this.polarisJ2000RA;
        this.polarisPrecessedRA = d + ((0.012812757700202005d + (0.005567398490379719d * Math.sin(Math.toRadians(d)) * Math.tan(Math.toRadians(this.polarisJ2000DEC)))) * (this.currentEpoch - 2000.0d));
    }

    public double getPrecessedRA() {
        return this.polarisPrecessedRA;
    }

    public double getPrecessedDEC() {
        return this.polarisPrecessedDEC;
    }

    private void calculateRigorousPrecession() {
        Coordinate coordinate = new Coordinate();
        Coordinate coordinate2 = new Coordinate();
        double d = (this.currentEpoch - 2000.0d) / 100.0d;
        coordinate.setPolar(1.0d, this.polarisJ2000DEC, this.polarisJ2000RA);
        double d2 = (((((0.017998d * d) + 0.30188d) * d) + 2306.2181d) * d) / 3600.0d;
        double d3 = (((((2.05E-4d * d) + 0.7928d) * d) * d) / 3600.0d) + d2;
        double d4 = ((2004.3109d - (((0.041833d * d) + 0.42665d) * d)) * d) / 3600.0d;
        double cos = cos(d3);
        double cos2 = cos(d4);
        double cos3 = cos(d2);
        double sin = sin(d3);
        double sin2 = sin(d4);
        double sin3 = sin(d2);
        double[][] dArr = (double[][]) Array.newInstance(double.class, new int[]{3, 3});
        double d5 = -sin;
        double d6 = cos * cos2;
        dArr[0][0] = (d5 * sin3) + (d6 * cos3);
        dArr[0][1] = (d5 * cos3) - (d6 * sin3);
        dArr[0][2] = (-cos) * sin2;
        double d7 = sin * cos2;
        dArr[1][0] = (cos * sin3) + (d7 * cos3);
        dArr[1][1] = (cos * cos3) - (d7 * sin3);
        dArr[1][2] = d5 * sin2;
        dArr[2][0] = cos3 * sin2;
        dArr[2][1] = (-sin2) * sin3;
        dArr[2][2] = cos2;
        coordinate2.setCartesian((dArr[0][0] * coordinate.getX()) + (dArr[0][1] * coordinate.getY()) + (dArr[0][2] * coordinate.getZ()), (dArr[1][0] * coordinate.getX()) + (dArr[1][1] * coordinate.getY()) + (dArr[1][2] * coordinate.getZ()), (dArr[2][0] * coordinate.getX()) + (dArr[2][1] * coordinate.getY()) + (dArr[2][2] * coordinate.getZ()));
        this.polarisPrecessedRA = coordinate2.getPhi();
        this.polarisPrecessedDEC = coordinate2.getTheta();
    }

    private double sin(double d) {
        return Math.sin(Math.toRadians(d));
    }

    private double cos(double d) {
        return Math.cos(Math.toRadians(d));
    }

    private double arcTan(double d) {
        return Math.toDegrees(Math.atan(d));
    }

    private double arcTan2(double d, double d2) {
        double degrees = Math.toDegrees(Math.atan2(d, d2));
        return degrees < 0.0d ? degrees + 360.0d : degrees;
    }

    private class Coordinate {

        private double phi;
        private double r;
        private double theta;
        private double x;
        private double y;
        private double z;

        private Coordinate() {
        }

        public void setCartesian(double d, double d2, double d3) {
            this.x = d;
            this.y = d2;
            this.z = d3;
            double d4 = (d * d) + (d2 * d2);
            this.r = Math.sqrt((d3 * d3) + d4);
            this.theta = PolarisPrecession.this.arcTan(d3 / Math.sqrt(d4));
            this.phi = PolarisPrecession.this.arcTan2(d2, d);
        }

        public void setPolar(double d, double d2, double d3) {
            this.r = d;
            this.theta = d2;
            this.phi = d3;
            this.x = PolarisPrecession.this.cos(d2) * d * PolarisPrecession.this.cos(d3);
            this.y = PolarisPrecession.this.cos(d2) * d * PolarisPrecession.this.sin(d3);
            this.z = d * PolarisPrecession.this.sin(d2);
        }

        public double getR() {
            return this.r;
        }

        public double getTheta() {
            return this.theta;
        }

        public double getPhi() {
            return this.phi;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}