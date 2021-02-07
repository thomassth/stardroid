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

package io.github.marcocipriani01.telescopetouch.source;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.StarsPrecession;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.AstronomicalSourceProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.GeocentricCoordinatesProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.LabelElementProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.LineElementProto;
import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto.PointElementProto;

/**
 * Implementation of the {@link AstronomicalSource} interface
 * from objects serialized as protocol buffers.
 *
 * @author Brent Bryan
 */
public class ProtobufAstronomicalSource extends AstronomicalSource {

    // Ideally we'd get this from Context.getPackageName but for some reason passing it in as a
    // string via the constructor results in it always being null when I need it. Buggered if
    // I know why - it's certainly a concern. Hopefully this class won't be around for much longer.
    public static final String PACKAGE = "io.github.marcocipriani01.telescopetouch";
    private static final String TAG = TelescopeTouchApp.getTag(ProtobufAstronomicalSource.class);
    private final AstronomicalSourceProto proto;
    private final Resources resources;
    // Lazily construct the names.
    private ArrayList<String> names;

    public ProtobufAstronomicalSource(AstronomicalSourceProto originalProto, Resources resources) {
        this.resources = resources;
        // Not ideal to be doing this in the constructor.
        // TODO(john): investigate which threads this is all happening on.
        this.proto = processStringIds(originalProto);
    }

    private static GeocentricCoordinates getCoords(GeocentricCoordinatesProto proto) {
        return StarsPrecession.precessGeocentric(Calendar.getInstance(), proto.getRightAscension(), proto.getDeclination());
    }

    /**
     * The data files contain only the text version of the string Ids. Looking them up
     * by this id will be expensive so pre-calculate any integer ids. See the data generation
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
            points.add(new PointSource(getCoords(element.getLocation()), element.getColor(), element.getSize()));
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
            points.add(new TextSource(getCoords(element.getLocation()),
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
            points.add(new LineSource(element.getColor(), vertices, element.getLineWidth()));
        }
        return points;
    }
}