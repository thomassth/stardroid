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

/**
 * Updates some aspect of the {@link AstronomerModel}.
 *
 * <p>Examples are: modifying the model's time, location or direction of
 * pointing.
 *
 * @author John Taylor
 */
public interface Controller {

    /**
     * Enables or disables this controller. When disabled the controller might
     * still be calculating updates, but won't pass them on to the model.
     */
    void setEnabled(boolean enabled);

    /**
     * Sets the {@link AstronomerModel} to be controlled by this controller.
     */
    void setModel(AstronomerModel model);

    /**
     * Starts this controller.
     *
     * <p>Called when the application is active.  Controllers that require
     * expensive resources such as sensor readings should obtain them when this is
     * called.
     */
    void start();

    /**
     * Stops this controller.
     *
     * <p>Called when the application or activity is inactive.  Controllers that
     * require expensive resources such as sensor readings should release them
     * when this is called.
     */
    void stop();
}