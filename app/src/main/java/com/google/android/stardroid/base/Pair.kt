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

import java.util.*

/**
 * A simple object which contains a pair of values.  This object can be stored and returned when
 * references to two objects are required.
 *
 * @author Brent Bryan
 */
class Pair<E, F>(first: E, second: F) {
    private var first: E
    private var second: F
    fun getFirst(): E {
        return first
    }

    fun setFirst(first: E) {
        this.first = first
    }

    fun getSecond(): F {
        return second
    }

    fun setSecond(second: F) {
        this.second = second
    }

    private class FirstComparator<E>(private val comparator: Comparator<E>) :
        Comparator<Pair<E, *>> {
        override fun compare(object1: Pair<E, *>, object2: Pair<E, *>): Int {
            return comparator.compare(object1.getFirst(), object2.getFirst())
        }
    }

    companion object {
        fun <S, T> of(first: S, second: T): Pair<S, T> {
            return Pair(first, second)
        }

        /**
         * Returns a new comparator which compares the first object in a set of pairs using the
         * specified Comparator.
         */
        fun <S> comparatorOfFirsts(comparator: Comparator<S>): Comparator<Pair<S, *>> {
            return FirstComparator(comparator)
        }
    }

    init {
        this.first = first
        this.second = second
    }
}