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

package io.github.marcocipriani01.telescopetouch.control;

import android.hardware.SensorManager;
import android.location.Location;

import java.util.Calendar;

import io.github.marcocipriani01.telescopetouch.astronomy.EquatorialCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.maths.Matrix3x3;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;

/**
 * The model of the astronomer.
 *
 * <p>Stores all the data about where and when he is and where he's looking and
 * handles translations between three frames of reference:
 * <ol>
 * <li>Celestial - a frame fixed against the background stars with
 * x, y, z axes pointing to (RA = 90, DEC = 0), (RA = 0, DEC = 0), DEC = 90
 * <li>Phone - a frame fixed in the phone with x across the short side, y across
 * the long side, and z coming out of the phone screen.
 * <li>Local - a frame fixed in the astronomer's local position. x is due east
 * along the ground y is due north along the ground, and z points towards the
 * zenith.
 * </ol>
 *
 * <p>We calculate the local frame in phone coords, and in celestial coords and
 * calculate a transform between the two.
 * In the following, N, E, U correspond to the local
 * North, East and Up vectors (ie N, E along the ground, Up to the Zenith)
 *
 * <p>In Phone Space: axesPhone = [N, E, U]
 *
 * <p>In Celestial Space: axesSpace = [N, E, U]
 *
 * <p>We find T such that axesCelestial = T * axesPhone
 *
 * <p>Then, [viewDir, viewUp]_celestial = T * [viewDir, viewUp]_phone
 *
 * <p>where the latter vector is trivial to calculate.
 *
 * <p>Implementation note: this class isn't making defensive copies and
 * so is vulnerable to clients changing its internal state.
 *
 * @author John Taylor
 */
public class AstronomerModel {

    private static final Vector3 POINTING_DIR_IN_PHONE_COORDS = new Vector3(0, 0, -1);
    private static final Vector3 SCREEN_UP_IN_PHONE_COORDS = new Vector3(0, 1, 0);
    private static final Vector3 SCREEN_DOWN_IN_PHONE_COORDS = new Vector3(1, 0, 0);
    private static final Vector3 AXIS_OF_EARTHS_ROTATION = new Vector3(0, 0, 1);
    private static final long MINIMUM_TIME_BETWEEN_CELESTIAL_COORD_UPDATES_MILLIS = 60000L;
    /**
     * The pointing comprises a vector into the phone's screen expressed in
     * celestial coordinates combined with a perpendicular vector along the
     * phone's longer side.
     */
    private final Pointing pointing = new Pointing();
    /**
     * The sensor acceleration in the phone's coordinate system.
     */
    private final Vector3 acceleration = new Vector3(0, -1, -9).copy();
    /**
     * The sensor magnetic field in the phone's coordinate system.
     */
    private final Vector3 magneticField = new Vector3(0, -1, 0).copy();
    private final float[] rotationVector = {1, 0, 0, 0};
    private Vector3 screenInPhoneCoords = SCREEN_UP_IN_PHONE_COORDS;
    private MagneticDeclinationCalculator magneticDeclinationCalculator;
    private boolean autoUpdatePointing = true;
    private float fieldOfView = 70.0f;  // Degrees
    private Location location = new Location("");
    private Clock clock = new RealClock();
    private long celestialCoordsLastUpdated = -1;
    private Vector3 upPhone = Vector3.scale(acceleration, -1);
    private boolean useRotationVector = false;
    /**
     * North along the ground in celestial coordinates.
     */
    private Vector3 trueNorthCelestial = new Vector3(1, 0, 0);
    /**
     * Up in celestial coordinates.
     */
    private Vector3 upCelestial = new Vector3(0, 1, 0);
    /**
     * East in celestial coordinates.
     */
    private Vector3 trueEastCelestial = AXIS_OF_EARTHS_ROTATION;
    /**
     * [North, Up, East]^-1 in phone coordinates.
     */
    private Matrix3x3 axesPhoneInverseMatrix = Matrix3x3.getIdMatrix();
    /**
     * [North, Up, East] in celestial coordinates.
     */
    private Matrix3x3 axesMagneticCelestialMatrix = Matrix3x3.getIdMatrix();

    /**
     * @param magneticDeclinationCalculator A calculator that will provide the
     *                                      magnetic correction from True North to Magnetic North.
     */
    public AstronomerModel(MagneticDeclinationCalculator magneticDeclinationCalculator) {
        setMagneticDeclinationCalculator(magneticDeclinationCalculator);
    }

    public void setHorizontalRotation(boolean value) {
        if (value) {
            screenInPhoneCoords = SCREEN_DOWN_IN_PHONE_COORDS;
        } else {
            screenInPhoneCoords = SCREEN_UP_IN_PHONE_COORDS;
        }
    }

    /**
     * If set to false, will not update the pointing automatically.
     */
    public void setAutoUpdatePointing(boolean autoUpdatePointing) {
        this.autoUpdatePointing = autoUpdatePointing;
    }

    /**
     * Gets the field of view in degrees.
     */
    public float getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(float degrees) {
        fieldOfView = degrees;
    }

    public float getMagneticCorrection() {
        return magneticDeclinationCalculator.getDeclination();
    }

    /**
     * Returns the time, as UTC.
     */
    public Calendar getTime() {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(clock.getTimeInMillisSinceEpoch());
        return instance;
    }

    /**
     * Returns the astronomer's current location on Earth.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the user's current position on Earth.
     */
    public void setLocation(Location location) {
        this.location = location;
        calculateLocalNorthAndUpInCelestialCoords(true);
    }

    /**
     * Gets the acceleration vector in the phone frame of reference.
     *
     * <p>The returned object should not be modified.
     */
    public Vector3 getPhoneUpDirection() {
        return upPhone;
    }

    /**
     * Sets the phone's rotation vector from the fused gyro/mag field/accelerometer.
     */
    public void setPhoneSensorValues(float[] rotationVector) {
        // TODO(jontayler): What checks do we need for this to be valid?
        // Note on some phones such as the Galaxy S4 this vector is the wrong size and needs to be truncated to 4.
        System.arraycopy(rotationVector, 0, this.rotationVector, 0, Math.min(rotationVector.length, 4));
        useRotationVector = true;
    }

    /**
     * Returns the user's North in celestial coordinates.
     */
    public GeocentricCoordinates getNorth() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstance(trueNorthCelestial);
    }

    /**
     * Returns the user's South in celestial coordinates.
     */
    public GeocentricCoordinates getSouth() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstance(Vector3.scale(trueNorthCelestial, -1));
    }

    /**
     * Returns the user's Zenith in celestial coordinates.
     */
    public GeocentricCoordinates getZenith() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstance(upCelestial);
    }

    /**
     * Returns the user's Nadir in celestial coordinates.
     */
    public GeocentricCoordinates getNadir() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstance(Vector3.scale(upCelestial, -1));
    }

    /**
     * Returns the user's East in celestial coordinates.
     */
    public GeocentricCoordinates getEast() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstance(trueEastCelestial);
    }

    /**
     * Returns the user's West in celestial coordinates.
     */
    public GeocentricCoordinates getWest() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstance(Vector3.scale(trueEastCelestial, -1));
    }

    public void setMagneticDeclinationCalculator(MagneticDeclinationCalculator calculator) {
        this.magneticDeclinationCalculator = calculator;
        calculateLocalNorthAndUpInCelestialCoords(true);
    }

    /**
     * Updates the astronomer's 'pointing', that is, the direction the phone is
     * facing in celestial coordinates and also the 'up' vector along the
     * screen (also in celestial coordinates).
     *
     * <p>This method requires that {@link #axesMagneticCelestialMatrix} and
     * {@link #axesPhoneInverseMatrix} are currently up to date.
     */
    private void calculatePointing() {
        if (autoUpdatePointing) {
            calculateLocalNorthAndUpInCelestialCoords(false);
            calculateLocalNorthAndUpInPhoneCoordsFromSensors();

            Matrix3x3 transform = Matrix3x3.matrixMultiply(axesMagneticCelestialMatrix, axesPhoneInverseMatrix);

            Vector3 viewInSpaceSpace = Matrix3x3.matrixVectorMultiply(transform, POINTING_DIR_IN_PHONE_COORDS);
            Vector3 screenUpInSpaceSpace = Matrix3x3.matrixVectorMultiply(transform, screenInPhoneCoords);

            pointing.updateLineOfSight(viewInSpaceSpace);
            pointing.updatePerpendicular(screenUpInSpaceSpace);
        }
    }

    public EquatorialCoordinates getEquatorialCoordinates() {
        return EquatorialCoordinates.getInstance(pointing.getLineOfSight());
    }

    /**
     * Calculates local North, East and Up vectors in terms of the celestial
     * coordinate frame.
     */
    private void calculateLocalNorthAndUpInCelestialCoords(boolean forceUpdate) {
        long currentTime = clock.getTimeInMillisSinceEpoch();
        if (!forceUpdate &&
                Math.abs(currentTime - celestialCoordsLastUpdated) < MINIMUM_TIME_BETWEEN_CELESTIAL_COORD_UPDATES_MILLIS)
            return;
        celestialCoordsLastUpdated = currentTime;
        magneticDeclinationCalculator.setLocationAndTime(location, getTimeMillis());
        upCelestial = GeocentricCoordinates.getInstance(EquatorialCoordinates.ofZenith(getTime(), location));
        Vector3 z = AXIS_OF_EARTHS_ROTATION;
        trueNorthCelestial = Vector3.sum(z, Vector3.scale(upCelestial, -Vector3.scalarProduct(upCelestial, z)));
        trueNorthCelestial.normalize();
        trueEastCelestial = Vector3.vectorProduct(trueNorthCelestial, upCelestial);

        // Apply magnetic correction.  Rather than correct the phone's axes for
        // the magnetic declination, it's more efficient to rotate the
        // celestial axes by the same amount in the opposite direction.
        Matrix3x3 rotationMatrix = Matrix3x3.calculateRotationMatrix(magneticDeclinationCalculator.getDeclination(), upCelestial);
        Vector3 magneticNorthCelestial = Matrix3x3.matrixVectorMultiply(rotationMatrix, trueNorthCelestial);
        Vector3 magneticEastCelestial = Vector3.vectorProduct(magneticNorthCelestial, upCelestial);
        axesMagneticCelestialMatrix = new Matrix3x3(magneticNorthCelestial, upCelestial, magneticEastCelestial);
    }

    // TODO(jontayler): with the switch to using the rotation vector sensor this is rather
    // convoluted and doing too much work.  It can be greatly simplified when we rewrite the rendering module.

    /**
     * Calculates local North and Up vectors in terms of the phone's coordinate
     * frame from the magnetic field and accelerometer sensors.
     */
    private void calculateLocalNorthAndUpInPhoneCoordsFromSensors() {
        Vector3 magneticNorthPhone;
        Vector3 magneticEastPhone;
        if (useRotationVector) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
            // The up and north vectors are the 2nd and 3rd rows of this matrix.
            magneticNorthPhone = new Vector3(rotationMatrix[3], rotationMatrix[4], rotationMatrix[5]);
            upPhone = new Vector3(rotationMatrix[6], rotationMatrix[7], rotationMatrix[8]);
            magneticEastPhone = new Vector3(rotationMatrix[0], rotationMatrix[1], rotationMatrix[2]);
        } else {
            // TODO(johntaylor): we can reduce the number of vector copies done in here.
            Vector3 down = acceleration.copy();
            down.normalize();
            // Magnetic field goes *from* North to South, so reverse it.
            Vector3 magneticFieldToNorth = magneticField.copy();
            magneticFieldToNorth.scale(-1);
            magneticFieldToNorth.normalize();
            // This is the vector to magnetic North *along the ground*.
            magneticNorthPhone = Vector3.sum(magneticFieldToNorth,
                    Vector3.scale(down, -Vector3.scalarProduct(magneticFieldToNorth, down)));
            magneticNorthPhone.normalize();
            upPhone = Vector3.scale(down, -1);
            magneticEastPhone = Vector3.vectorProduct(magneticNorthPhone, upPhone);
        }
        // The matrix is orthogonal, so transpose it to find its inverse.
        // Easiest way to do that is to construct it from row vectors instead
        // of column vectors.
        axesPhoneInverseMatrix = new Matrix3x3(magneticNorthPhone, upPhone, magneticEastPhone, false);
    }

    /**
     * Returns the user's pointing.  Note that clients should not usually modify this
     * object as it is not defensively copied.
     */
    public Pointing getPointing() {
        calculatePointing();
        return pointing;
    }

    /**
     * Sets the user's direction of view.
     */
    public void setPointing(Vector3 lineOfSight, Vector3 perpendicular) {
        this.pointing.updateLineOfSight(lineOfSight);
        this.pointing.updatePerpendicular(perpendicular);
    }

    /**
     * Sets the clock that provides the time.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
        calculateLocalNorthAndUpInCelestialCoords(true);
    }

    public long getTimeMillis() {
        return clock.getTimeInMillisSinceEpoch();
    }
}