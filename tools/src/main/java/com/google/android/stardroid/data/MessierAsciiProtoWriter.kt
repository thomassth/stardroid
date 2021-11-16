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
import java.io.IOException

/**
 * Class for reading the messier csv file and writing the contents to protocol
 * buffers.
 *
 * @author brent@google.com (Brent Bryan)
 */
class MessierAsciiProtoWriter : AbstractAsciiProtoWriter() {
    override fun getSourceFromLine(line: String, index: Int): AstronomicalSourceProto? {
        // name, type, RA(h), dec(degrees), magnitude, size, ngc, constellation,
        // names, common name
        // Of these, only name(0), ra(2), & dec(3) are used.
        if (line.startsWith("Object,Type")) {
            return null
        }

        // TODO(brent): Add image shapes here?
        val tokens = line.split(",").toTypedArray()

        // Convert from hours to degrees.
        val ra = 15 * tokens[2].toFloat()
        val dec = tokens[3].toFloat()
        val magnitude = 4.9f
        val sourceBuilder: AstronomicalSourceProto.Builder = AstronomicalSourceProto.newBuilder()
        val coords: SourceProto.GeocentricCoordinatesProto? = getCoords(ra, dec)
        val labelBuilder: SourceProto.LabelElementProto.Builder = SourceProto.LabelElementProto.newBuilder()
        labelBuilder.setColor(LABEL_COLOR)
        labelBuilder.setLocation(coords)
        val rKeysForName = rKeysFromName(tokens[0])
        if (!rKeysForName!!.isEmpty()) {
            labelBuilder.setStringsStrId(rKeysForName[0])
        }
        sourceBuilder.addLabel(labelBuilder)
        val pointBuilder: SourceProto.PointElementProto.Builder = SourceProto.PointElementProto.newBuilder()
        pointBuilder.setColor(POINT_COLOR)
        pointBuilder.setLocation(coords)
        pointBuilder.setSize(POINT_SIZE)
        // TODO(johntaylor): consider setting messier object shape
        sourceBuilder.addPoint(pointBuilder)
        for (rKey in rKeysForName) {
            sourceBuilder.addNameStrIds(rKey)
        }
        sourceBuilder.setSearchLocation(coords)
        return sourceBuilder.build()
    }

    companion object {
        // TODO(mrhector): verify colors
        private const val LABEL_COLOR = 0x48a841 // argb
        private const val POINT_COLOR = 0x48a841 // abgr (!)
        private const val POINT_SIZE = 3
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            MessierAsciiProtoWriter().run(args)
        }
    }
}