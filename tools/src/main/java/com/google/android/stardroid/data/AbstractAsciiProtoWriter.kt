// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.data

import com.google.android.stardroid.source.proto.SourceProto
import com.google.android.stardroid.source.proto.SourceProto.AstronomicalSourceProto
import com.google.common.io.Closeables
import java.io.*
import java.util.*

/**
 * Base class for converting (ASCII) text data files into protocol buffers.
 * This class writes out a human readable version of the
 * messages with place holders for the text strings.
 *
 * @author Brent Bryan
 */
abstract class AbstractAsciiProtoWriter {
    /**
     * Returns the AstronomicalSource associated with the given line, or null if
     * the line does not correspond to a valid [AstronomicalSource].
     */
    protected abstract fun getSourceFromLine(line: String, index: Int): AstronomicalSourceProto?

    /**
     * Gets the list of string IDs for the object names (that is, of the
     * form R.string.foo).
     * @param names pipe-separated object names
     */
    protected fun rKeysFromName(names: String): List<String> {
        val rNames: MutableList<String> = ArrayList()
        for (name in names.split(NAME_DELIMITER).toTypedArray()) {
            rNames.add(name.replace(" ".toRegex(), "_").toLowerCase())
        }
        return rNames
    }

    protected fun getCoords(ra: Float, dec: Float): SourceProto.GeocentricCoordinatesProto {
        return SourceProto.GeocentricCoordinatesProto.newBuilder()
            .setRightAscension(ra)
            .setDeclination(dec)
            .build()
    }

    @Throws(IOException::class)
    fun readSources(`in`: BufferedReader): SourceProto.AstronomicalSourcesProto {
        val builder: SourceProto.AstronomicalSourcesProto.Builder = SourceProto.AstronomicalSourcesProto.newBuilder()
        var line: String
        while (`in`.readLine().also { line = it } != null) {
            line = line.trim { it <= ' ' }
            if (line.isEmpty()) {
                continue
            }
            val source: AstronomicalSourceProto? = getSourceFromLine(line, builder.getSourceCount())
            if (source != null) {
                builder.addSource(source)
            }
        }
        return builder.build()
    }

    @Throws(IOException::class)
    fun writeFiles(prefix: String, sources: SourceProto.AstronomicalSourcesProto) {

        /*FileOutputStream out = null;
    try {
      out = new FileOutputStream(prefix + ".binary");
      sources.writeTo(out);
    } finally {
      Closeables.closeSilently(out);
    }*/
        var writer: PrintWriter? = null
        try {
            writer = PrintWriter(FileWriter("$prefix.ascii"))
            writer.append(sources.toString())
        } finally {
            Closeables.close(writer, false)
        }
        System.out.println(
            "Successfully wrote " + sources.getSourceCount().toString() + " sources."
        )
    }

    @Throws(IOException::class)
    fun run(args: Array<String>) {
        if (args.size != 2) {
            System.out.printf("Usage: %s <inputfile> <outputprefix>", this.javaClass.canonicalName)
            System.exit(1)
        }
        args[0] = args[0].trim { it <= ' ' }
        args[1] = args[1].trim { it <= ' ' }
        println("Input File: " + args[0])
        println("Output Prefix: " + args[1])
        BufferedReader(FileReader(args[0])).use { `in` -> writeFiles(args[1], readSources(`in`)) }
    }

    companion object {
        private const val NAME_DELIMITER = "[|]+"
    }
}