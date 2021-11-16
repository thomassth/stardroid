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

import java.util.*

/**
 * A Priority Queue implementation which holds no more than a specified number
 * of elements. When the queue size exceeds this specified size, the root is
 * removed to ensure that the resulting size remains fixed.
 *
 * @param <E> type of object contained in the queue
 *
 * @author Brent Bryan
</E> */
class FixedSizePriorityQueue<E>(
    /** Maximum number of elements stored in this queue.  */
    private val maxSize: Int, comparator: Comparator<in E>?
) : PriorityQueue<E>(
    maxSize, comparator
) {
    /**
     * Returns the filter that is currently being used to reject elements which
     * are submitted for addition to the queue. Returns null if not filter has
     * been set.
     */
    /**
     * Sets the filter used to reject objects (without checking the number of
     * elements in the queue, or their priorities). Setting the filter to null
     * removes all filtering.
     */
    /**
     * Filter used to reject some objects without even checking the number of
     * objects or priorities of those objects in the queue.
     */
    var filter: Filter<in E>? = null
    override fun add(`object`: E): Boolean {
        if (filter != null && !filter!!.accept(`object`)) {
            return false
        }
        if (!isFull) {
            super.add(`object`)
            return true
        }
        if (comparator().compare(`object`, peek()) > 0) {
            poll()
            super.add(`object`)
            return true
        }
        return false
    }

    override fun addAll(c: Collection<E>): Boolean {
        var changed = false
        for (e in c) {
            changed = changed or add(e)
        }
        return changed
    }

    val isFull: Boolean
        get() = size == maxSize

    companion object {
        private const val serialVersionUID = 3959389634971824728L
    }
}