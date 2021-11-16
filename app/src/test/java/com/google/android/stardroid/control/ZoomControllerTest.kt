// Copyright 2009 Google Inc.
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
package com.google.android.stardroid.control

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.google.android.stardroid.control.AstronomerModel
import com.google.android.stardroid.control.ZoomController
import org.junit.Before
import kotlin.Throws
import org.easymock.EasyMock
import com.google.android.stardroid.control.ZoomControllerTest
import junit.framework.TestCase
import com.google.android.stardroid.control.TransitioningCompositeClock
import com.google.android.stardroid.control.TimeTravelClock
import com.google.android.stardroid.control.TransitioningCompositeClockTest.FakeClock
import org.junit.Test
import org.robolectric.annotation.Config
import java.lang.Exception

/**
 * Test suite for the [ZoomController].
 *
 * @author John Taylor
 *
 * Tests that require roboelectric for API calls.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ZoomControllerTest {
    private var astronomerModel: AstronomerModel? = null
    private var zoomController: ZoomController? = null
    @Before
    @Throws(Exception::class)
    fun setUp() {
        astronomerModel = EasyMock.createMock(AstronomerModel::class.java)
        zoomController = ZoomController()
        zoomController!!.setModel(astronomerModel)
    }

    /**
     * Tests that the maximum field of view is not exceeded.
     */
    @Test
    fun testZoomOut_tooFar() {
        val newFieldOfView = ZoomController.MAX_ZOOM_OUT
        EasyMock.expect(astronomerModel!!.fieldOfView).andStubReturn(INITIAL_FIELD_OF_VIEW)
        astronomerModel!!.fieldOfView = newFieldOfView
        EasyMock.replay(astronomerModel)
        zoomController!!.zoomBy(1000f)
        EasyMock.verify(astronomerModel)
    }

    @Test
    fun testZoomIn_modelNotUpdatedWhenControllerNotEnabled() {
        EasyMock.expect(astronomerModel!!.fieldOfView).andReturn(INITIAL_FIELD_OF_VIEW)
        // Note that setFieldOfView will not be called
        EasyMock.replay(astronomerModel)
        zoomController!!.setEnabled(false)
        zoomController!!.zoomBy(0.9f)
        EasyMock.verify(astronomerModel)
    }

    companion object {
        private const val INITIAL_FIELD_OF_VIEW = 30.0f
    }
}