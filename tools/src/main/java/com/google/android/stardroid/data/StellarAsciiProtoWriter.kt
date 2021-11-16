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

import android.graphics.Color
import com.google.android.stardroid.source.proto.SourceProto
import com.google.android.stardroid.source.proto.SourceProto.AstronomicalSourceProto
import com.google.android.stardroid.util.StarAttributeCalculator
import java.io.IOException

/**
 * Class for reading the stellar csv file and writing the contents to a protocol
 * buffer.
 *
 * @author Brent Bryan
 */
class StellarAsciiProtoWriter : AbstractAsciiProtoWriter() {
    override fun getSourceFromLine(line: String, count: Int): AstronomicalSourceProto? {
        // name, mag, dec, ra
        val tokens = line.split(",").toTypedArray()
        if (tokens.size != 7) {
            throw RuntimeException("Found " + tokens.size + ".  Expected 7.")
        }
        val name = tokens[0]
        val magnitude = tokens[1].toFloat()
        val dec = tokens[2].toFloat()
        val ra = tokens[3].toFloat()
        if (magnitude >= StarAttributeCalculator.MAX_MAGNITUDE) {
            return null
        }
        val color: Int = StarAttributeCalculator.getColor(magnitude, Color.WHITE)
        val size: Int = StarAttributeCalculator.getSize(magnitude)
        val builder: AstronomicalSourceProto.Builder = AstronomicalSourceProto.newBuilder()
        val pointBuilder: SourceProto.PointElementProto.Builder = SourceProto.PointElementProto.newBuilder()
        pointBuilder.setColor(color)
        val coords: SourceProto.GeocentricCoordinatesProto? = getCoords(ra, dec)
        pointBuilder.setLocation(coords)
        pointBuilder.setSize(size)
        builder.addPoint(pointBuilder)
        if (name != null && !name.trim { it <= ' ' }.isEmpty()) {
            val labelBuilder: SourceProto.LabelElementProto.Builder = SourceProto.LabelElementProto.newBuilder()
            labelBuilder.setColor(STAR_COLOR)
            labelBuilder.setLocation(getCoords(ra, dec))
            val rKeysForName = rKeysFromName(name)
            if (!rKeysForName!!.isEmpty()) {
                labelBuilder.setStringsStrId(rKeysForName[0])
            }
            builder.addLabel(labelBuilder)
            for (rKey in rKeysForName) {
                builder.addNameStrIds(rKey)
            }
        }
        builder.setSearchLocation(coords)
        return builder.build()
    }

    companion object {
        private const val STAR_COLOR = 0xcfcccf
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            StellarAsciiProtoWriter().run(args)
        }
    }
}