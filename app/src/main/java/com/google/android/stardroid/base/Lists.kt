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
 * Utility methods for easily dealing with Lists.
 *
 * @author Brent Bryan
 */
object Lists {
    /**
     * Transforms each element in the given Iterable and returns the result as a
     * List. Does not change the given Iterable, or the items stored therein.
     */
    fun <E, F> transform(iterable: Iterable<E>, transform: Transform<E, F>): List<F> {
        val result: MutableList<F> = ArrayList()
        for (e in iterable) {
            result.add(transform.transform(e))
        }
        return result
    }

    /**
     * Returns the given Iterable as a List. If the current Iterable is already a
     * List, then the Iterable is returned directly. Otherwise a new List is
     * created with the same elements as the given Iterable.
     */
    fun <E> asList(iterable: Iterable<E>): List<E> {
        if (iterable is List<*>) {
            return iterable as List<E>
        }
        val result: MutableList<E> = ArrayList()
        for (e in iterable) {
            result.add(e)
        }
        return result
    }

    /**
     * Converts a user specified set of objects into a [List] of that type.
     */
    @JvmStatic
    fun <E> asList(vararg objects: E): List<E> {
        return Arrays.asList(*objects)
    }
}