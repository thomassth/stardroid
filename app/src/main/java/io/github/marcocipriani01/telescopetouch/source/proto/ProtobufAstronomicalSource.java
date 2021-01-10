package io.github.marcocipriani01.telescopetouch.source.proto;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.source.AbstractAstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;
import io.github.marcocipriani01.telescopetouch.source.impl.LineSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.PointSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.impl.TextSourceImpl;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.AstronomicalSourceProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.GeocentricCoordinatesProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.LabelElementProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.LineElementProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.PointElementProto;
import io.github.marcocipriani01.telescopetouch.units.GeocentricCoordinates;

/**
 * Implementation of the
 * {@link AstronomicalSource} interface
 * from objects serialized as protocol buffers.
 *
 * @author Brent Bryan
 */
public class ProtobufAstronomicalSource extends AbstractAstronomicalSource {

    // Ideally we'd get this from Context.getPackageName but for some reason passing it in as a
    // string via the contructor results in it always being null when I need it. Buggered if
    // I know why - it's certainly a concern. Hopefully this class won't be around for much longer.
    public static final String PACKAGE = "io.github.marcocipriani01.telescopetouch";
    private static final String TAG = TelescopeTouchApp.getTag(ProtobufAstronomicalSource.class);
    private static final Map<SourceProto.Shape, PointSource.Shape> shapeMap = new HashMap<>();

    static {
        shapeMap.put(SourceProto.Shape.CIRCLE, PointSource.Shape.CIRCLE);
        shapeMap.put(SourceProto.Shape.STAR, PointSource.Shape.CIRCLE);
        shapeMap.put(SourceProto.Shape.ELLIPTICAL_GALAXY, PointSource.Shape.ELLIPTICAL_GALAXY);
        shapeMap.put(SourceProto.Shape.SPIRAL_GALAXY, PointSource.Shape.SPIRAL_GALAXY);
        shapeMap.put(SourceProto.Shape.IRREGULAR_GALAXY, PointSource.Shape.IRREGULAR_GALAXY);
        shapeMap.put(SourceProto.Shape.LENTICULAR_GALAXY, PointSource.Shape.LENTICULAR_GALAXY);
        shapeMap.put(SourceProto.Shape.GLOBULAR_CLUSTER, PointSource.Shape.GLOBULAR_CLUSTER);
        shapeMap.put(SourceProto.Shape.OPEN_CLUSTER, PointSource.Shape.OPEN_CLUSTER);
        shapeMap.put(SourceProto.Shape.NEBULA, PointSource.Shape.NEBULA);
        shapeMap.put(SourceProto.Shape.HUBBLE_DEEP_FIELD, PointSource.Shape.HUBBLE_DEEP_FIELD);
    }

    private final AstronomicalSourceProto proto;
    private final Resources resources;

    // Lazily construct the names.
    private ArrayList<String> names;

    public ProtobufAstronomicalSource(AstronomicalSourceProto originalProto, Resources resources) {
        this.resources = resources;
        // Not ideal to be doing this in the constructor. TODO(john): investigate which threads
        // this is all happening on.
        this.proto = processStringIds(originalProto);
    }

    private static GeocentricCoordinates getCoords(GeocentricCoordinatesProto proto) {
        return GeocentricCoordinates.getInstance(proto.getRightAscension(), proto.getDeclination());
    }

    /**
     * The data files contain only the text version of the string Ids. Looking them up
     * by this id will be expensive so precalculate any integer ids. See the datageneration
     * design doc for an explanation.
     */
    private AstronomicalSourceProto processStringIds(AstronomicalSourceProto proto) {
        AstronomicalSourceProto.Builder processed = proto.toBuilder();
        for (String strId : proto.getNameStrIdsList()) {
            processed.addNameIntIds(toInt(strId));
        }
        // <rant>
        // Work around Google's clumsy protocol buffer API. For some inexplicable reason the current
        // version lacks the getFooBuilderList described here:
        // https://developers.google.com/protocol-buffers/docs/reference/java-generated#fields
        // </rant>
        List<LabelElementProto> newLabels = new ArrayList<>(processed.getLabelCount());
        for (LabelElementProto label : processed.getLabelList()) {
            LabelElementProto.Builder labelBuilder = label.toBuilder();
            labelBuilder.setStringsIntId(toInt(label.getStringsStrId()));
            newLabels.add(labelBuilder.build());
        }
        processed.clearLabel();
        processed.addAllLabel(newLabels);
        return processed.build();
    }

    private int toInt(String stringId) {
        int resourceId = resources.getIdentifier(stringId, "string", PACKAGE);
        return resourceId == 0 ? R.string.missing_label : resourceId;
    }

    @Override
    public synchronized ArrayList<String> getNames() {
        if (names == null) {
            names = new ArrayList<>(proto.getNameIntIdsCount());
            for (int id : proto.getNameIntIdsList()) {
                names.add(resources.getString(id));
            }
        }
        return names;
    }

    @Override
    public GeocentricCoordinates getSearchLocation() {
        return getCoords(proto.getSearchLocation());
    }

    @Override
    public List<PointSource> getPoints() {
        if (proto.getPointCount() == 0) {
            return Collections.emptyList();
        }
        ArrayList<PointSource> points = new ArrayList<>(proto.getPointCount());
        for (PointElementProto element : proto.getPointList()) {
            points.add(new PointSourceImpl(getCoords(element.getLocation()),
                    element.getColor(), element.getSize(), shapeMap.get(element.getShape())));
        }
        return points;
    }

    @Override
    public List<TextSource> getLabels() {
        if (proto.getLabelCount() == 0) {
            return Collections.emptyList();
        }
        ArrayList<TextSource> points = new ArrayList<>(proto.getLabelCount());
        for (LabelElementProto element : proto.getLabelList()) {
            Log.d(TAG, "Label " + element.getStringsIntId() + " : " + element.getStringsStrId());
            points.add(new TextSourceImpl(getCoords(element.getLocation()),
                    resources.getString(element.getStringsIntId()),
                    element.getColor(), element.getOffset(), element.getFontSize()));
        }
        return points;
    }

    @Override
    public List<LineSource> getLines() {
        if (proto.getLineCount() == 0) {
            return Collections.emptyList();
        }
        ArrayList<LineSource> points = new ArrayList<>(proto.getLineCount());
        for (LineElementProto element : proto.getLineList()) {
            ArrayList<GeocentricCoordinates> vertices =
                    new ArrayList<>(element.getVertexCount());
            for (GeocentricCoordinatesProto elementVertex : element.getVertexList()) {
                vertices.add(getCoords(elementVertex));
            }
            points.add(new LineSourceImpl(element.getColor(), vertices, element.getLineWidth()));
        }
        return points;
    }
}