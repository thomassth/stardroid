// Copyright 2008 Google Inc.
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
package com.google.android.stardroid.renderer

import android.content.res.Resources
import com.google.android.stardroid.R
import com.google.android.stardroid.math.MathUtils.acos
import com.google.android.stardroid.math.MathUtils.atan2
import com.google.android.stardroid.math.MathUtils.sqrt
import com.google.android.stardroid.math.TWO_PI
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.renderer.util.SearchHelper
import com.google.android.stardroid.renderer.util.TextureManager
import com.google.android.stardroid.renderer.util.TextureReference
import com.google.android.stardroid.renderer.util.TexturedQuad
import com.google.android.stardroid.util.FixedPoint
import com.google.android.stardroid.util.FixedPoint.floatToFixedPoint
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI

class SearchArrow {
    // The arrow quad is 10% of the screen width or height, whichever is smaller.
    private val ARROW_SIZE = 0.1f

    // The circle quad is 40% of the screen width or height, whichever is smaller.
    private val CIRCLE_SIZE = 0.4f

    // The target position is (1, theta, phi) in spherical coordinates.
    private var mTargetTheta = 0f
    private var mTargetPhi = 0f
    private var mCircleQuad: TexturedQuad? = null
    private var mArrowQuad: TexturedQuad? = null
    private var mArrowOffset = 0f
    private var mCircleSizeFactor = 1f
    private var mArrowSizeFactor = 1f
    private var mFullCircleScaleFactor = 1f
    private var mArrowTex: TextureReference? = null
    private var mCircleTex: TextureReference? = null
    fun reloadTextures(gl: GL10, res: Resources?, textureManager: TextureManager) {
        gl.glEnable(GL10.GL_TEXTURE_2D)
        mArrowTex = textureManager.getTextureFromResource(gl, R.drawable.arrow)
        mCircleTex = textureManager.getTextureFromResource(gl, R.drawable.arrowcircle)
        gl.glDisable(GL10.GL_TEXTURE_2D)
    }

    fun resize(gl: GL10?, screenWidth: Int, screenHeight: Int, fullCircleSize: Float) {
        mArrowSizeFactor = ARROW_SIZE * Math.min(screenWidth, screenHeight)
        mArrowQuad = TexturedQuad(
            mArrowTex,
            0f, 0f, 0f,
            0.5f, 0f, 0f,
            0f, 0.5f, 0f
        )
        mFullCircleScaleFactor = fullCircleSize
        mCircleSizeFactor = CIRCLE_SIZE * mFullCircleScaleFactor
        mCircleQuad = TexturedQuad(
            mCircleTex,
            0f, 0f, 0f,
            0.5f, 0f, 0f,
            0f, 0.5f, 0f
        )
        mArrowOffset = mCircleSizeFactor + mArrowSizeFactor
    }

    fun draw(
        gl: GL10, lookDir: Vector3, upDir: Vector3, searchHelper: SearchHelper,
        nightVisionMode: Boolean
    ) {
        val lookPhi = acos(lookDir.y)
        val lookTheta = atan2(lookDir.z, lookDir.x)

        // Positive diffPhi means you need to look up.
        val diffPhi = lookPhi - mTargetPhi

        // Positive diffTheta means you need to look right.
        var diffTheta = lookTheta - mTargetTheta

        // diffTheta could potentially be in the range from (-2*Pi, 2*Pi), but we need it
        // in the range (-Pi, Pi).
        if (diffTheta > PI) {
            diffTheta -= TWO_PI
        } else if (diffTheta < -PI) {
            diffTheta += TWO_PI
        }

        // The image I'm using is an arrow pointing right, so an angle of 0 corresponds to that. 
        // This is why we're taking arctan(diffPhi / diffTheta), because diffTheta corresponds to
        // the amount we need to rotate in the xz plane and diffPhi in the up direction.
        var angle = atan2(diffPhi, diffTheta)

        // Need to add on the camera roll, which is the amount you need to rotate the vector (0, 1, 0)
        // about the look direction in order to get it in the same plane as the up direction.
        val roll = angleBetweenVectorsWithRespectToAxis(Vector3(0f, 1f, 0f), upDir, lookDir)
        angle += roll

        // Distance is a normalized value of the distance.
        val distance: Float = (1.0f / (1.414f * PI) *
                sqrt(diffTheta * diffTheta + diffPhi * diffPhi)).toFloat()
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glPushMatrix()
        gl.glRotatef((angle * 180.0f / PI).toFloat(), 0f, 0f, -1f)
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND.toFloat())

        // 0 means the circle is not expanded at all.  1 means fully expanded.
        val expandFactor = searchHelper.transitionFactor
        if (expandFactor == 0f) {
            gl.glColor4x(FixedPoint.ONE, FixedPoint.ONE, FixedPoint.ONE, FixedPoint.ONE)
            val redFactor: Float
            val blueFactor: Float
            if (nightVisionMode) {
                redFactor = 0.6f
                blueFactor = 0f
            } else {
                redFactor = 1.0f - distance
                blueFactor = distance
            }
            gl.glTexEnvfv(
                GL10.GL_TEXTURE_ENV,
                GL10.GL_TEXTURE_ENV_COLOR,
                floatArrayOf(redFactor, 0.0f, blueFactor, 0.0f),
                0
            )
            gl.glPushMatrix()
            val circleScale = mCircleSizeFactor
            gl.glScalef(circleScale, circleScale, circleScale)
            mCircleQuad!!.draw(gl)
            gl.glPopMatrix()
            gl.glPushMatrix()
            val arrowScale = mArrowSizeFactor
            gl.glTranslatef(mArrowOffset * 0.5f, 0f, 0f)
            gl.glScalef(arrowScale, arrowScale, arrowScale)
            mArrowQuad!!.draw(gl)
            gl.glPopMatrix()
        } else {
            gl.glColor4x(
                FixedPoint.ONE, FixedPoint.ONE, FixedPoint.ONE,
                floatToFixedPoint(0.7f)
            )
            gl.glTexEnvfv(
                GL10.GL_TEXTURE_ENV,
                GL10.GL_TEXTURE_ENV_COLOR,
                floatArrayOf(1f, (if (nightVisionMode) 0 else 0.5f) as Float, 0f, 0.0f),
                0
            )
            gl.glPushMatrix()
            val circleScale = mFullCircleScaleFactor * expandFactor +
                    mCircleSizeFactor * (1 - expandFactor)
            gl.glScalef(circleScale, circleScale, circleScale)
            mCircleQuad!!.draw(gl)
            gl.glPopMatrix()
        }
        gl.glPopMatrix()
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE.toFloat())
        gl.glDisable(GL10.GL_BLEND)
    }

    fun setTarget(position: Vector3) {
        var position = position
        position = position.normalizedCopy()
        mTargetPhi = acos(position.y)
        mTargetTheta = atan2(position.z, position.x)
    }

    companion object {
        // Given vectors v1 and v2, and an axis, this function returns the angle which you must rotate v1
        // by in order for it to be in the same plane as v2 and axis.  Assumes that all vectors are unit
        // vectors and v2 and axis are perpendicular.
        private fun angleBetweenVectorsWithRespectToAxis(
            v1: Vector3,
            v2: Vector3,
            axis: Vector3
        ): Float {
            // Make v1 perpendicular to axis.  We want an orthonormal basis for the plane perpendicular
            // to axis.  After rotating v1, the projection of v1 and v2 into this plane should be equal.
            var v1proj = v1.minus(v1.projectOnto(axis))
            v1proj = v1proj.normalizedCopy()

            // Get the vector perpendicular to the one you're rotating and the axis.  Since axis and v1proj
            // are orthonormal, this one must be a unit vector perpendicular to all three.
            val perp = axis.times(v1proj)

            // v2 is perpendicular to axis, so therefore it's already in the same plane as v1proj perp.
            val cosAngle = v1proj.dot(v2)
            val sinAngle = -perp.dot(v2)
            return atan2(sinAngle, cosAngle)
        }
    }
}