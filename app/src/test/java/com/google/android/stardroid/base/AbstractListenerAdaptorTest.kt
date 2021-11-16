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
import java.util.HashSet

/**
 * Unittests for the AbstractListenerAdaptor.
 *
 * @author Brent Bryan
 */
class AbstractListenerAdaptorTest : TestCase() {
    private val adaptor = SimpleListenerAdapter()

    /** A simple listener that just responds if an event has fired.  */
    private interface SimpleListener {
        /** Indicates that an Event has fired.  */
        fun eventFired()
    }

    /**
     * A Simple concrete implementation of an AbstractListenerAdapator for use in
     * testing.
     */
    private class SimpleListenerAdapter : AbstractListenerAdaptor<SimpleListener?>() {
        protected override fun fireNewListenerAdded(listener: SimpleListener) {
            listener.eventFired()
        }
    }

    /**
     * Creates a new mock SimpleListener that will expect to have eventFired call
     * numEventsFired number of times. If numEventsFired is negative, then the
     * mock will expect that eventFired to be called any number of times.
     *
     * @param numEventsFired number of times the mock should expect eventFired to
     * be called. If numEvents is negative, then the mock will allow
     * eventFired to be called any number of times.
     * @return a new mock SimpleListener expecting eventFired to be called the
     * specified number of times.
     */
    private fun createMockSimpleListener(numEventsFired: Int): SimpleListener {
        val listener = EasyMock.createMock(SimpleListener::class.java)
        listener.eventFired()
        if (numEventsFired < 0) {
            EasyMock.expectLastCall<Any>().anyTimes()
        } else {
            EasyMock.expectLastCall<Any>().times(numEventsFired)
        }
        EasyMock.replay(listener)
        return listener
    }

    /**
     * Ensures that when adding a listener that the count of the number of
     * listeners is correctly incremented and that eventFired is called
     * immediately on the listener.
     */
    fun testAddListener() {
        val listener1 = createMockSimpleListener(1)
        val listener2 = createMockSimpleListener(1)
        assertEquals(0, adaptor.numListeners)
        adaptor.addListener(listener1)
        assertEquals(1, adaptor.numListeners)
        adaptor.addListener(listener2)
        assertEquals(2, adaptor.numListeners)
        adaptor.addListener(listener1)
        assertEquals(2, adaptor.numListeners)
        val listeners: List<SimpleListener> = Lists.asList(adaptor.listeners)
        assertEquals(2, listeners.size)
        assertTrue(listeners.contains(listener1))
        assertTrue(listeners.contains(listener2))
        EasyMock.verify(listener1, listener2)
    }

    fun testRemoveListener() {
        val listener1 = createMockSimpleListener(1)
        val listener2 = createMockSimpleListener(1)
        assertEquals(0, adaptor.numListeners)
        adaptor.addListener(listener1)
        adaptor.addListener(listener2)
        assertEquals(2, adaptor.numListeners)
        adaptor.removeListener(listener1)
        assertEquals(1, adaptor.numListeners)
        adaptor.removeListener(listener1)
        assertEquals(1, adaptor.numListeners)
        val listeners: List<SimpleListener> = Lists.asList(adaptor.listeners)
        assertEquals(1, listeners.size)
        assertEquals(listener2, listeners[0])
        EasyMock.verify(listener1, listener2)
    }

    fun testRemoveAllListeners() {
        val listener1 = createMockSimpleListener(1)
        val listener2 = createMockSimpleListener(1)
        assertEquals(0, adaptor.numListeners)
        adaptor.addListener(listener1)
        adaptor.addListener(listener2)
        assertEquals(2, adaptor.numListeners)
        adaptor.removeAllListeners()
        assertEquals(0, adaptor.numListeners)
        assertTrue(Lists.asList(adaptor.listeners).isEmpty())
        EasyMock.verify(listener1, listener2)
    }

    fun testGetListeners() {
        val expectedListeners = HashSet<SimpleListener>()
        for (i in 0..3) {
            val listener = createMockSimpleListener(1)
            expectedListeners.add(listener)
            assertEquals(i, adaptor.numListeners)
            adaptor.addListener(listener)
            assertEquals(i + 1, adaptor.numListeners)
        }
        val observedListeners: List<SimpleListener> = Lists.asList(adaptor.listeners)
        assertEquals(4, observedListeners.size)
        for (listener in observedListeners) {
            assertTrue(
                String.format("observed unexpected listener: %s", listener),
                expectedListeners.remove(listener)
            )
            EasyMock.verify(listener)
        }
        assertTrue(expectedListeners.isEmpty())
    }
}