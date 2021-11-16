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
package com.google.android.stardroid.util

import junit.framework.TestCase
import java.util.*

/**
 * Unit tests for the WeakHashSet class.
 *
 * @author John Taylor
 */
class WeakHashSetTest : TestCase() {
    private var set: WeakHashSet<String>? = null
    public override fun setUp() {
        set = WeakHashSet()
    }

    /**
     * Test method for
     * [com.google.android.stardroid.util.WeakHashSet.size].
     */
    fun testSize() {
        val one = "one"
        set!!.add(one)
        val two = "two"
        set!!.add(two)
        assertEquals(2, set!!.size)
        set!!.clear()
        assertTrue(set!!.isEmpty())
        assertEquals(0, set!!.size)
    }

    /**
     * Test method for
     * [com.google.android.stardroid.util.WeakHashSet.add]
     * .
     */
    fun testAdd() {
        val one = "one"
        set!!.add(one)
        assertTrue(set!!.contains(one))
        assertFalse(set!!.contains("two"))
    }

    /**
     * Test add two identical items.
     */
    fun testAddTwo() {
        val one = "one"
        assertTrue(set!!.add(one))
        assertFalse(set!!.add(one))
        assertTrue(set!!.contains(one))
        assertEquals(1, set!!.size)
    }

    /**
     * Test that the references are weak. Note that this test might prove to be
     * flaky, since garbage collection cannot be guaranteed.
     *
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    fun testWeak() {
        val objectSet: MutableSet<Any> = WeakHashSet()
        objectSet.add(Any())
        // Try to force some garbage collection
        for (i in 0..999999) {
            Any()
        }
        System.gc()
        Thread.sleep(100)
        assertTrue(objectSet.isEmpty())
    }

    /**
     * Test method for
     * [java.util.AbstractSet.removeAll].
     */
    fun testRemoveAll() {
        val one = "one"
        val two = "two"
        val three = "three"
        set!!.add(one)
        set!!.add(two)
        set!!.add(three)
        val list: MutableList<String> = ArrayList()
        list.add(one)
        list.add(three)
        set!!.removeAll(list)
        assertEquals(1, set!!.size)
        assertTrue(set!!.contains(two))
    }
}