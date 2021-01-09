package io.github.marcocipriani01.telescopetouch.data;

import com.google.protobuf.TextFormat;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto;

/**
 * Class for writing Ascii protocol buffers in binary format.
 *
 * @author Brent Bryan
 */
public class AsciiToBinaryProtoWriter {

    public static void main(String[] args) throws IOException {
        if (args.length != 1 || !args[0].endsWith(".ascii")) {
            System.out.println("Usage: AsciiToBinaryProtoWriter <inputprefix>.ascii");
            System.exit(1);
        }
        try (FileReader in = new FileReader(args[0]);
             FileOutputStream out = new FileOutputStream(args[0].substring(0, args[0].length() - 6) + ".binary")) {
            SourceProto.AstronomicalSourcesProto.Builder builder = SourceProto.AstronomicalSourcesProto.newBuilder();
            TextFormat.merge(in, builder);
            SourceProto.AstronomicalSourcesProto sources = builder.build();
            System.out.println("Source count " + sources.getSourceCount());
            sources.writeTo(out);
        }
    }
}