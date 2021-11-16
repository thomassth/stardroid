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
package com.google.android.stardroid.base

import junit.framework.TestCase
import com.google.android.stardroid.base.ListsTest.TestEnum
import com.google.android.stardroid.base.FixedSizePriorityQueue
import kotlin.Throws
import com.google.android.stardroid.base.AbstractListenerAdaptorTest.SimpleListenerAdapter
import com.google.android.stardroid.base.AbstractListenerAdaptor
import com.google.android.stardroid.base.AbstractListenerAdaptorTest.SimpleListener
import org.easymock.EasyMock

/**
 * Unit tests for the Pair class.
 *
 * @author Brent Bryan
 */
class PairTest : TestCase() {
    fun testCreatePair() {
        val p = Pair("ThreePointOne", 3.1)
        assertEquals("ThreePointOne", p.first)
        assertEquals(3.1, p.second.toDouble())
    }

    fun testOf() {
        val p = Pair.of(3, "Three")
        assertEquals(3, p.first.toInt())
        assertEquals("Three", p.second)
    }

    fun testNull() {
        val p = Pair.of<Int?, String?>(null, null)
        assertEquals(null, p.first)
        assertEquals(null, p.second)
    }

    fun testSetters() {
        val p = Pair.of<Int?, Double?>(null, null)
        p.first = 2
        assertEquals(2, p.first!!.toInt())
        assertEquals(null, p.second)
        p.second = 3.4
        assertEquals(2, p.first!!.toInt())
        assertEquals(3.4, p.second!!.toDouble())
    }
}