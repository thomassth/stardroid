/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.renderer;

import android.os.ConditionVariable;
import android.util.Log;

import java.util.EnumSet;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.renderer.util.UpdateClosure;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

public abstract class RendererControllerBase {

    private static final boolean SHOULD_LOG_QUEUE = false;
    // TODO(brent): collapse these into a single class?
    private static final boolean SHOULD_LOG_RUN = false;
    private static final boolean SHOULD_LOG_FINISH = false;
    protected final SkyRenderer mRenderer;

    public RendererControllerBase(SkyRenderer renderer) {
        mRenderer = renderer;
    }

    protected static void queueRunnable(EventQueuer queuer, final String msg, final CommandType type, final Runnable r) {
        // If we're supposed to log something, then wrap the runnable with the
        // appropriate logging statements.  Otherwise, just queue it.
        if (SHOULD_LOG_QUEUE || SHOULD_LOG_RUN || SHOULD_LOG_FINISH) {
            logQueue(msg, type);
            queuer.queueEvent(() -> {
                logRun(msg, type);
                r.run();
                logFinish(msg, type);
            });
        } else {
            queuer.queueEvent(r);
        }
    }

    protected static void logQueue(String description, CommandType type) {
        if (SHOULD_LOG_QUEUE) {
            Log.d("RendererController-" + type.toString(), "Queuing: " + description);
        }
    }

    protected static void logRun(String description, CommandType type) {
        if (SHOULD_LOG_RUN) {
            Log.d("RendererController-" + type.toString(), "Running: " + description);
        }
    }

    protected static void logFinish(String description, CommandType type) {
        if (SHOULD_LOG_FINISH) {
            Log.d("RendererController-" + type.toString(), "Finished: " + description);
        }
    }

    public PointManager createPointManager(int layer) {
        PointManager manager = new PointManager(mRenderer.createPointManager(layer));
        queueAddManager(manager);
        return manager;
    }

    public LineManager createLineManager(int layer) {
        LineManager manager = new LineManager(mRenderer.createPolyLineManager(layer));
        queueAddManager(manager);
        return manager;
    }

    public LabelManager createLabelManager(int layer) {
        LabelManager manager = new LabelManager(mRenderer.createLabelManager(layer));
        queueAddManager(manager);
        return manager;
    }

    public ImageManager createImageManager(int layer) {
        ImageManager manager = new ImageManager(mRenderer.createImageManager(layer));
        queueAddManager(manager);
        return manager;
    }

    public void queueNightVisionMode(final boolean enable) {
        queueRunnable("Setting night vision mode: " + enable,
                CommandType.View, () -> mRenderer.setNightVisionMode(enable));
    }

    public void queueFieldOfView(final float fov) {
        queueRunnable("Setting fov: " + fov, CommandType.View, () -> mRenderer.setRadiusOfView(fov));
    }

    public void queueTextAngle(final float angleInRadians) {
        queueRunnable("Setting text angle: " + angleInRadians,
                CommandType.View, () -> mRenderer.setTextAngle(angleInRadians));
    }

    public void queueViewerUpDirection(final GeocentricCoordinates up) {
        queueRunnable("Setting up direction: " + up, CommandType.View, () -> mRenderer.setViewerUpDirection(up));
    }

    public void queueSetViewOrientation(final float dirX, final float dirY, final float dirZ,
                                        final float upX, final float upY, final float upZ) {
        queueRunnable("Setting view orientation",
                CommandType.Data, () -> mRenderer.setViewOrientation(dirX, dirY, dirZ, upX, upY, upZ));
    }

    public void queueEnableSkyGradient(final GeocentricCoordinates sunPosition) {
        queueRunnable("Enabling sky gradient at: " + sunPosition,
                CommandType.Data, () -> mRenderer.enableSkyGradient(sunPosition));
    }

    public void queueDisableSkyGradient() {
        queueRunnable("Disabling sky gradient",
                CommandType.Data, mRenderer::disableSkyGradient);
    }

    public void queueEnableSearchOverlay(final GeocentricCoordinates target, final String targetName) {
        queueRunnable("Enabling search overlay",
                CommandType.Data, () -> mRenderer.enableSearchOverlay(target, targetName));
    }

    public void queueDisableSearchOverlay() {
        queueRunnable("Disabling search overlay", CommandType.Data, mRenderer::disableSearchOverlay);
    }

    public void addUpdateClosure(final UpdateClosure runnable) {
        queueRunnable("Setting update callback", CommandType.Data, () -> mRenderer.addUpdateClosure(runnable));
    }

    public void removeUpdateCallback(final UpdateClosure update) {
        queueRunnable("Removing update callback", CommandType.Data, () -> mRenderer.removeUpdateCallback(update));
    }

    /**
     * Must be called once to register an object manager to the renderer.
     */
    public <E> void queueAddManager(final RenderManager<E> rom) {
        queueRunnable("Adding manager: " + rom, CommandType.Data, () -> mRenderer.addObjectManager(rom.manager));
    }

    public void waitUntilFinished() {
        final ConditionVariable cv = new ConditionVariable();
        queueRunnable("Waiting until operations have finished", CommandType.Synchronization, cv::open);
        cv.block();
    }

    abstract protected EventQueuer getQueuer();

    protected void queueRunnable(String msg, final CommandType type, final Runnable r) {
        RendererControllerBase.queueRunnable(getQueuer(), toString() + " - " + msg, type, r);
    }

    // Used only to allow logging different types of events.  The distinction
    // can be somewhat ambiguous at times, so when in doubt, I tend to use
    // "view" for those things that change all the time (like the direction
    // the user is looking) and "data" for those that change less often
    // (like whether a layer is visible or not).
    protected enum CommandType {
        View,  // The command only changes the user's view.
        Data,  // The command changes what is actually rendered.
        Synchronization  // The command relates to synchronization.
    }

    protected interface EventQueuer {
        void queueEvent(Runnable r);
    }

    /**
     * Base class for all renderer managers.
     */
    public static abstract class RenderManager<E> {

        final protected RendererObjectManager manager;

        private RenderManager(RendererObjectManager mgr) {
            manager = mgr;
        }

        public void queueEnabled(final boolean enable, RendererControllerBase controller) {
            final String msg = (enable ? "Enabling" : "Disabling") + " manager " + manager;
            controller.queueRunnable(msg, CommandType.Data, () -> manager.enable(enable));
        }

        public abstract void queueObjects(
                final List<E> objects,
                final EnumSet<RendererObjectManager.UpdateType> updateType,
                RendererControllerBase controller);
    }

    /**
     * Class for managing a set of point objects.
     */
    public static class PointManager extends RenderManager<PointSource> {

        private PointManager(PointObjectManager manager) {
            super(manager);
        }

        @Override
        public void queueObjects(final List<PointSource> points,
                                 final EnumSet<RendererObjectManager.UpdateType> updateType,
                                 RendererControllerBase controller) {
            String msg = "Setting point objects";
            controller.queueRunnable(msg, CommandType.Data, () -> ((PointObjectManager) manager).updateObjects(points, updateType));
        }
    }

    /**
     * Class for managing a set of polyline objects.
     */
    public static class LineManager extends RenderManager<LineSource> {

        private LineManager(PolyLineObjectManager manager) {
            super(manager);
        }

        @Override
        public void queueObjects(final List<LineSource> lines,
                                 final EnumSet<RendererObjectManager.UpdateType> updateType,
                                 RendererControllerBase controller) {
            String msg = "Setting line objects";
            controller.queueRunnable(msg, CommandType.Data, () -> ((PolyLineObjectManager) manager).updateObjects(lines, updateType));
        }
    }

    /**
     * Class for managing a set of text label objects.
     */
    public static class LabelManager extends RenderManager<TextSource> {
        private LabelManager(LabelObjectManager manager) {
            super(manager);
        }

        @Override
        public void queueObjects(final List<TextSource> labels,
                                 final EnumSet<RendererObjectManager.UpdateType> updateType,
                                 RendererControllerBase controller) {
            String msg = "Setting label objects";
            controller.queueRunnable(msg, CommandType.Data, () -> ((LabelObjectManager) manager).updateObjects(labels, updateType));
        }
    }

    /**
     * Class for managing a set of image objects.
     */
    public static class ImageManager extends RenderManager<ImageSource> {
        private ImageManager(ImageObjectManager manager) {
            super(manager);
        }

        @Override
        public void queueObjects(final List<ImageSource> images,
                                 final EnumSet<RendererObjectManager.UpdateType> updateType,
                                 RendererControllerBase controller) {
            String msg = "Setting image objects";
            controller.queueRunnable(msg, CommandType.Data, () -> ((ImageObjectManager) manager).updateObjects(images, updateType));
        }
    }
}