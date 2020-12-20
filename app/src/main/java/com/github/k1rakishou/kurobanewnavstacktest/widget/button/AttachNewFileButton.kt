package com.github.k1rakishou.kurobanewnavstacktest.widget.button

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp


class AttachNewFileButton @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : View(context, attributeSet, attrDefStyle) {

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var addIconDrawable: Drawable

  init {
    setWillNotDraw(false)

    addIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_add_24)!!.mutate()
    DrawableCompat.setTint(addIconDrawable, Color.GRAY)
    paint.color = Color.GRAY

    if (!isInEditMode) {
      init(Color.RED, 4.dp.toFloat())
    } else {
      init(Color.RED, 8f)
    }
  }

  private fun init(color: Int, width: Float) {
    paint.style = Paint.Style.STROKE
    paint.color = color
    paint.strokeWidth = width
    paint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
  }

  override fun onDraw(canvas: Canvas) {
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    val centerX = width / 2
    val centerY = height / 2

    addIconDrawable.setBounds(
      centerX - (ADD_ICON_SIZE / 2),
      centerY - (ADD_ICON_SIZE / 2),
      centerX + (ADD_ICON_SIZE / 2),
      centerY + (ADD_ICON_SIZE / 2)
    )

    addIconDrawable.draw(canvas)
  }

  companion object {
    private var ADD_ICON_SIZE = 32.dp
  }
}