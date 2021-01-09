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