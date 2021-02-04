package io.github.marcocipriani01.telescopetouch.data;

import android.graphics.Color;

import java.io.IOException;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto;

/**
 * Class for reading the stellar csv file and writing the contents to a protocol
 * buffer.
 *
 * @author Brent Bryan
 */
public class StellarAsciiProtoWriter extends AbstractAsciiProtoWriter {
    private static final int STAR_COLOR = 0xcfcccf;

    public static void main(String[] args) throws IOException {
        new StellarAsciiProtoWriter().run(args);
    }

    @Override
    protected SourceProto.AstronomicalSourceProto getSourceFromLine(String line, int count) {
        // name, mag, dec, ra
        String[] tokens = line.split(",");
        if (tokens.length != 7) {
            throw new RuntimeException("Found " + tokens.length + ".  Expected 7.");
        }

        String name = tokens[0];
        float magnitude = Float.parseFloat(tokens[1]);
        float dec = Float.parseFloat(tokens[2]);
        float ra = Float.parseFloat(tokens[3]);

        if (magnitude >= StarAttributeCalculator.MAX_MAGNITUDE) {
            return null;
        }

        int color = StarAttributeCalculator.getColor(magnitude, Color.WHITE);
        int size = StarAttributeCalculator.getSize(magnitude);
        SourceProto.AstronomicalSourceProto.Builder builder = SourceProto.AstronomicalSourceProto.newBuilder();

        SourceProto.PointElementProto.Builder pointBuilder = SourceProto.PointElementProto.newBuilder();
        pointBuilder.setColor(color);
        SourceProto.GeocentricCoordinatesProto coords = getCoords(ra, dec);
        pointBuilder.setLocation(coords);
        pointBuilder.setSize(size);
        builder.addPoint(pointBuilder);

        if (name != null && !name.trim().isEmpty()) {
            SourceProto.LabelElementProto.Builder labelBuilder = SourceProto.LabelElementProto.newBuilder();
            labelBuilder.setColor(STAR_COLOR);
            labelBuilder.setLocation(getCoords(ra, dec));
            List<String> rKeysForName = rKeysFromName(name);
            if (!rKeysForName.isEmpty()) {
                labelBuilder.setStringsStrId(rKeysForName.get(0));
            }
            builder.addLabel(labelBuilder);
            for (String rKey : rKeysForName) {
                builder.addNameStrIds(rKey);
            }
        }
        builder.setSearchLocation(coords);
        return builder.build();
    }
}