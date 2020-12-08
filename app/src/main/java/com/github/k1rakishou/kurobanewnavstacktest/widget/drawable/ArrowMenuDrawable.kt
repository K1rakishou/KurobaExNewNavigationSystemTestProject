/*
 * Kuroba - *chan browser https://github.com/Adamantcheese/Kuroba/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.k1rakishou.kurobanewnavstacktest.widget.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp

class ArrowMenuDrawable : Drawable() {
  private val mPaint = Paint()

  // The thickness of the bars
  private val mBarThickness: Float = 2f.dp

  // The length of top and bottom bars when they merge into an arrow
  private val mTopBottomArrowSize: Float = 11.31f.dp

  // The length of middle bar
  private val mBarSize: Float = 18f.dp

  // The length of the middle bar when arrow is shaped
  private val mMiddleArrowSize: Float = 16f.dp

  // The space between bars when they are parallel
  private val mBarGap: Float = 3f.dp

  // Use Path instead of canvas operations so that if color has transparency, overlapping sections
  // wont look different
  private val mPath = Path()

  // The reported intrinsic size of the drawable.
  val mSize: Int = 24.dp

  // Whether we should mirror animation when animation is reversed.
  private var mVerticalMirror = false

  // The interpolated version of the original progress
  private var mProgress = 0.0f
  private var badgeText: String? = null
  private var badgeRed = false
  private val badgePaint = Paint()
  private val badgeTextBounds = Rect()

  var progress: Float
    get() = mProgress

    set(progress) {
      if (progress != mProgress) {
        if (progress.compareTo(1f) == 0) {
          mVerticalMirror = true
        } else if (progress.compareTo(0f) == 0) {
          mVerticalMirror = false
        }

        mProgress = progress
        invalidateSelf()
      }
    }

  init {
    mPaint.color = Color.WHITE
    mPaint.isAntiAlias = true
    mPaint.style = Paint.Style.STROKE
    mPaint.strokeJoin = Paint.Join.MITER
    mPaint.strokeCap = Paint.Cap.SQUARE
    mPaint.strokeWidth = mBarThickness
    badgePaint.isAntiAlias = true
  }

  override fun draw(canvas: Canvas) {
    val bounds: Rect = bounds
    // Interpolated widths of arrow bars
    val arrowSize = lerp(mBarSize, mTopBottomArrowSize, mProgress)
    val middleBarSize = lerp(mBarSize, mMiddleArrowSize, mProgress)
    // Interpolated size of middle bar
    val middleBarCut = lerp(0f, mBarThickness / 2, mProgress)
    // The rotation of the top and bottom bars (that make the arrow head)
    val rotation = lerp(0f, ARROW_HEAD_ANGLE, mProgress)

    // The whole canvas rotates as the transition happens
    val canvasRotate = lerp(-180f, 0f, mProgress)
    val topBottomBarOffset = lerp(mBarGap + mBarThickness, 0f, mProgress)
    mPath.rewind()
    val arrowEdge = -middleBarSize / 2
    // draw middle bar
    mPath.moveTo(arrowEdge + middleBarCut, 0f)
    mPath.rLineTo(middleBarSize - middleBarCut, 0f)
    var arrowWidth = arrowSize * Math.cos(rotation.toDouble()).toFloat()
    var arrowHeight = arrowSize * Math.sin(rotation.toDouble()).toFloat()

    if (mProgress.compareTo(0f) == 0 || mProgress.compareTo(1f) == 0) {
      arrowWidth = Math.round(arrowWidth).toFloat()
      arrowHeight = Math.round(arrowHeight).toFloat()
    }

    // top bar
    mPath.moveTo(arrowEdge, topBottomBarOffset)
    mPath.rLineTo(arrowWidth, arrowHeight)

    // bottom bar
    mPath.moveTo(arrowEdge, -topBottomBarOffset)
    mPath.rLineTo(arrowWidth, -arrowHeight)

    canvas.save()
    // Rotate the whole canvas if spinning.
    canvas.rotate(
      canvasRotate * if (mVerticalMirror) -1 else 1,
      bounds.centerX().toFloat(),
      bounds.centerY().toFloat()
    )

    canvas.translate(bounds.centerX().toFloat(), bounds.centerY().toFloat())
    canvas.drawPath(mPath, mPaint)
    canvas.restore()

    // Draw a badge over the arrow/menu
    if (badgeText != null) {
      canvas.save()

      val badgeSize = intrinsicWidth * 0.7f
      val badgeX = intrinsicWidth - badgeSize / 2f
      val badgeY = badgeSize / 2f

      if (badgeRed) {
        badgePaint.color = -0x220bbcca
      } else {
        badgePaint.color = -0x77000000
      }

      canvas.drawCircle(badgeX, badgeY, badgeSize / 2f, badgePaint)

      val textSize = when (badgeText!!.length) {
        1 -> badgeSize * 0.7f
        2 -> badgeSize * 0.6f
        else -> badgeSize * 0.5f
      }

      badgePaint.color = Color.WHITE
      badgePaint.textSize = textSize
      badgePaint.getTextBounds(badgeText, 0, badgeText!!.length, badgeTextBounds)
      canvas.drawText(
        badgeText!!,
        badgeX - badgeTextBounds.right / 2f,
        badgeY - badgeTextBounds.top / 2f,
        badgePaint
      )
      canvas.restore()
    }
  }

  override fun setAlpha(i: Int) {
    mPaint.alpha = i
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    mPaint.colorFilter = colorFilter
  }

  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  companion object {
    // The angle in degrees that the arrow head is inclined at.
    private val ARROW_HEAD_ANGLE = Math.toRadians(45.0).toFloat()

    /**
     * Linear interpolate between a and b with parameter t.
     */
    private fun lerp(a: Float, b: Float, t: Float): Float {
      return a + (b - a) * t
    }
  }

}