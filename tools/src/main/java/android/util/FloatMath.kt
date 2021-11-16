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
package android.util

/**
 * @author Brent Bryan
 */
object FloatMath {
    fun cos(x: Float): Float {
        return Math.cos(x.toDouble()).toFloat()
    }

    fun sin(x: Float): Float {
        return Math.sin(x.toDouble()).toFloat()
    }

    fun tan(x: Float): Float {
        return Math.tan(x.toDouble()).toFloat()
    }

    fun sqrt(x: Float): Float {
        return Math.sqrt(x.toDouble()).toFloat()
    }

    fun floor(x: Float): Float {
        return Math.floor(x.toDouble()).toFloat()
    }

    fun ceil(x: Float): Float {
        return Math.ceil(x.toDouble()).toFloat()
    }
}