package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.google.protobuf.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApplication;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.proto.ProtobufAstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.AstronomicalSourceProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.AstronomicalSourcesProto;

/**
 * Implementation of the {@link Layer} interface which reads its data from
 * a file during the {@link Layer#initialize} method.
 *
 * @author Brent Bryan
 * @author John Taylor
 */
public abstract class AbstractFileBasedLayer extends AbstractLayer {

    private static final String TAG = TelescopeTouchApplication.getTag(AbstractFileBasedLayer.class);
    private static final Executor BACKGROUND_EXECUTOR = Executors.newFixedThreadPool(1);

    private final AssetManager assetManager;
    private final String fileName;
    private final List<AstronomicalSource> fileSources = new ArrayList<>();

    public AbstractFileBasedLayer(AssetManager assetManager, Resources resources, String fileName) {
        super(resources, false);
        this.assetManager = assetManager;
        this.fileName = fileName;
    }

    @Override
    public synchronized void initialize() {
        BACKGROUND_EXECUTOR.execute(() -> {
            readSourceFile(fileName);
            AbstractFileBasedLayer.super.initialize();
        });
    }

    @Override
    protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
        sources.addAll(fileSources);
    }

    private void readSourceFile(String sourceFilename) {
        Log.d(TAG, "Loading Proto File: " + sourceFilename + "...");
        try (InputStream in = assetManager.open(sourceFilename, AssetManager.ACCESS_BUFFER)) {
            Parser<AstronomicalSourcesProto> parser = AstronomicalSourcesProto.parser();
            AstronomicalSourcesProto sources = parser.parseFrom(in);
            for (AstronomicalSourceProto proto : sources.getSourceList()) {
                fileSources.add(new ProtobufAstronomicalSource(proto, getResources()));
            }
            Log.d(TAG, "Found: " + fileSources.size() + " sources");
            String s = String.format("Finished Loading: %s | Found %s sourcs.\n",
                    sourceFilename, fileSources.size());
            Log.d(TAG, s);
            refreshSources(EnumSet.of(UpdateType.Reset));
        } catch (IOException e) {
            Log.e(TAG, "Unable to open " + sourceFilename);
        }
    }
}