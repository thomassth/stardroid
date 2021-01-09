package io.github.marcocipriani01.telescopetouch.units;

import io.github.marcocipriani01.telescopetouch.util.Geometry;
import io.github.marcocipriani01.telescopetouch.util.MathUtil;

/**
 * A simple struct for latitude and longitude.
 */
public class LatLong {
    private float latitude;
    private float longitude;

    public LatLong(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        // Silently enforce reasonable limits
        if (this.latitude > 90f) {
            this.latitude = 90f;
        }
        if (this.latitude < -90f) {
            this.latitude = -90f;
        }
        this.longitude = flooredMod(this.longitude + 180f, 360f) - 180f;
    }

    /**
     * This constructor automatically downcasts the latitude and longitude to
     * floats, so that the previous constructor can be used. It is added as a
     * convenience method, since many of the GPS methods return doubles.
     */
    public LatLong(double latitude, double longitude) {
        this((float) latitude, (float) longitude);
    }

    /**
     * Returns the 'floored' mod assuming n>0.
     */
    private static float flooredMod(float a, float n) {
        return (a < 0 ? a % n + n : a) % n;
    }

    /**
     * Angular distance between the two points.
     *
     * @param other
     * @return degrees
     */
    public float distanceFrom(LatLong other) {
        // Some misuse of the astronomy math classes
        GeocentricCoordinates otherPnt = GeocentricCoordinates.getInstance(other.longitude,
                other.latitude);
        GeocentricCoordinates thisPnt = GeocentricCoordinates.getInstance(this.longitude,
                this.latitude);
        float cosTheta = Geometry.cosineSimilarity(thisPnt, otherPnt);
        return MathUtil.acos(cosTheta) * 180f / MathUtil.PI;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
