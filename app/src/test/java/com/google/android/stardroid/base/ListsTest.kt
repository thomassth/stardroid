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
import java.lang.RuntimeException
import java.util.*

/**
 * Unittests for the Lists class.
 *
 * @author Brent Bryan
 */
class ListsTest : TestCase() {
    private enum class TestEnum {
        ONE, TWO, THREE
    }

    fun testTransform() {
        val set = EnumSet.of(TestEnum.TWO, TestEnum.ONE)
        val list: List<Int> = Lists.transform(set, object : Transform<TestEnum?, Int?> {
            override fun transform(e: TestEnum): Int {
                return when (e) {
                    TestEnum.ONE -> 1
                    TestEnum.TWO -> 2
                    TestEnum.THREE -> 3
                }
                throw RuntimeException()
            }
        })
        assertEquals(2, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
    }

    fun testAsList_fromNonList() {
        val set = EnumSet.of(TestEnum.TWO, TestEnum.ONE)
        val list = Lists.asList(set)
        assertEquals(2, list.size)
        assertEquals(TestEnum.ONE, list[0])
        assertEquals(TestEnum.TWO, list[1])
    }

    fun testAsList_fromList() {
        val startList = Arrays.asList(*arrayOf(2, 4, 1))
        val newList = Lists.asList(startList)
        assertEquals(3, newList.size)
        assertEquals(2, newList[0].toInt())
        assertEquals(4, newList[1].toInt())
        assertEquals(1, newList[2].toInt())
        assertTrue(startList === newList)
    }
}