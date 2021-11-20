// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.layers

import android.content.res.Resources
import android.graphics.Color
import com.google.android.stardroid.R
import com.google.android.stardroid.base.Lists.asList
import com.google.android.stardroid.base.TimeConstants
import com.google.android.stardroid.control.AstronomerModel
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.renderer.RendererObjectManager.UpdateType
import com.google.android.stardroid.source.*
import java.util.*

/**
 * Creates a mark at the zenith, nadir and cardinal point and a horizon.
 *
 * @author Brent Bryan
 * @author John Taylor
 */
class HorizonLayer(private val model: AstronomerModel, resources: Resources?) : AbstractSourceLayer(
    resources!!, true
) {
    override fun initializeAstroSources(sources: ArrayList<AstronomicalSource>) {
        sources.add(HorizonSource(model, resources))
    }

    override val layerDepthOrder: Int
        get() = 90

    // TODO(brent): Remove this.
    override val preferenceId: String
        get() = "source_provider.5"

    // TODO(johntaylor): i18n
    override val layerName: String
        get() =// TODO(johntaylor): i18n
            "Horizon"

    override fun getLayerNameId(): Int {
        return R.string.show_horizon_pref // TODO(johntaylor): rename this string id
    }

    /** Implementation of [AstronomicalSource] for the horizon source.  */
    internal class HorizonSource(private val model: AstronomerModel, res: Resources) :
        AbstractAstronomicalSource() {
        private val zenith = Vector3(0f, 0f, 0f)
        private val nadir = Vector3(0f, 0f, 0f)
        private val north = Vector3(0f, 0f, 0f)
        private val south = Vector3(0f, 0f, 0f)
        private val east = Vector3(0f, 0f, 0f)
        private val west = Vector3(0f, 0f, 0f)
        private val linePrimitives = ArrayList<LinePrimitive>()
        private val textPrimitives = ArrayList<TextPrimitive>()
        private var lastUpdateTimeMs = 0L
        private fun updateCoords() {
            // Blog.d(this, "Updating Coords: " + (model.getTime().getTime() - lastUpdateTimeMs));
            lastUpdateTimeMs = model.time.time
            zenith.assign(model.zenith)
            nadir.assign(model.nadir)
            north.assign(model.north)
            south.assign(model.south)
            east.assign(model.east)
            west.assign(model.west)
        }

        override fun initialize(): Sources? {
            updateCoords()
            return this
        }

        override fun update(): EnumSet<UpdateType?>? {
            val updateTypes = EnumSet.noneOf(UpdateType::class.java)

            // TODO(brent): Add distance here.
            if (Math.abs(model.time.time - lastUpdateTimeMs) > UPDATE_FREQ_MS) {
                updateCoords()
                updateTypes.add(UpdateType.UpdatePositions)
            }
            return updateTypes
        }

        override val labels: List<TextPrimitive>
            get() = textPrimitives
        override val lines: List<LinePrimitive>
            get() = linePrimitives

        companion object {
            // Due to a bug in the G1 rendering code text and lines render in different
            // colors.
            private val LINE_COLOR = Color.argb(120, 86, 176, 245)
            private val LABEL_COLOR = Color.argb(120, 245, 176, 86)
            private const val UPDATE_FREQ_MS = 1L * TimeConstants.MILLISECONDS_PER_SECOND
        }

        init {
            val vertices: List<Vector3?> = asList(north, east, south, west, north)
            linePrimitives.add(LinePrimitive(LINE_COLOR, vertices, 1.5f))
            textPrimitives.add(TextPrimitive(zenith, res.getString(R.string.zenith), LABEL_COLOR))
            textPrimitives.add(TextPrimitive(nadir, res.getString(R.string.nadir), LABEL_COLOR))
            textPrimitives.add(TextPrimitive(north, res.getString(R.string.north), LABEL_COLOR))
            textPrimitives.add(TextPrimitive(south, res.getString(R.string.south), LABEL_COLOR))
            textPrimitives.add(TextPrimitive(east, res.getString(R.string.east), LABEL_COLOR))
            textPrimitives.add(TextPrimitive(west, res.getString(R.string.west), LABEL_COLOR))
        }
    }
}