// Copyright 2009 Google Inc.
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
import java.lang.Exception
import java.util.*

/**
 * Unit tests for the FixedSizePriorityQueue class.
 *
 * @author Brent Bryan
 */
class FixedSizePriorityQueueTest : TestCase() {
    var queue: FixedSizePriorityQueue<Int>? = null
    var evenFilter: Filter<Int>? = null
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val comparator = Comparator<Int> { object1, object2 ->
            if (object1 == object2) {
                return@Comparator 0
            }
            if (object1 < object2) -1 else 1
        }
        queue = FixedSizePriorityQueue(10, comparator)
        evenFilter = object : Filter<Int?> {
            override fun accept(`object`: Int): Boolean {
                return `object` % 2 == 0
            }
        }
    }

    fun testAdd_noFilter() {
        assertEquals(null, queue!!.filter)
        assertEquals(0, queue!!.size)
        for (i in 0..9) {
            assertFalse(queue!!.isFull)
            assertTrue(queue!!.add(i))
            assertEquals(i + 1, queue!!.size)
        }
        assertEquals(0, queue!!.peek().toInt())
        assertTrue(queue!!.isFull)

        // Queue is full, should not be able to add a new lesser value
        assertFalse(queue!!.add(-1))
        assertEquals(0, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        assertFalse(queue!!.isEmpty())
        assertTrue(queue!!.isFull)
        assertTrue(queue!!.add(10))
        assertEquals(1, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        assertTrue(queue!!.add(5))
        assertEquals(2, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
    }

    fun testAdd_filter() {
        queue!!.filter = evenFilter
        val numbers = arrayOf(6, 8, 10, 12, 14, 16, 18, 20, 22)
        assertTrue(queue!!.addAll(Arrays.asList(*numbers)))
        assertEquals(6, queue!!.peek().toInt())
        assertEquals(9, queue!!.size)
        assertFalse(queue!!.add(5))
        assertEquals(6, queue!!.peek().toInt())
        assertEquals(9, queue!!.size)
        assertTrue(queue!!.add(4))
        assertEquals(4, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        assertFalse(queue!!.add(2))
        assertEquals(4, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        assertFalse(queue!!.add(11))
        assertEquals(4, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
    }

    fun testAddAll_noFilter() {
        assertEquals(null, queue!!.filter)
        assertTrue(queue!!.isEmpty())
        val numbers = arrayOf(2, 3, 4, 5, 10, 12, 18, 20)
        assertTrue(queue!!.addAll(Arrays.asList(*numbers)))
        assertEquals(2, queue!!.peek().toInt())
        assertEquals(8, queue!!.size)
        assertFalse(queue!!.isEmpty())
        assertFalse(queue!!.isFull)
        val moreNumbers = arrayOf(1, 7, 9)
        assertTrue(queue!!.addAll(Arrays.asList(*moreNumbers)))
        assertEquals(2, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        assertTrue(queue!!.isFull)
        val evenMoreNumbers = arrayOf(0, 1, 0)
        assertFalse(queue!!.addAll(Arrays.asList(*evenMoreNumbers)))
        assertEquals(2, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
    }

    fun testAddAll_filter() {
        queue!!.filter = evenFilter
        assertNotNull(queue!!.filter)
        val numbers = arrayOf(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
        assertTrue(queue!!.addAll(Arrays.asList(*numbers)))
        assertEquals(2, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        val moreNumbers = arrayOf(0, 6, 13)
        assertTrue(queue!!.addAll(Arrays.asList(*moreNumbers)))
        assertEquals(4, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
        val evenMoreNumbers = arrayOf(1, 11, 31)
        assertFalse(queue!!.addAll(Arrays.asList(*evenMoreNumbers)))
        assertEquals(4, queue!!.peek().toInt())
        assertEquals(10, queue!!.size)
    }
}