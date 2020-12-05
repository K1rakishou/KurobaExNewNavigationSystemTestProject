package com.github.k1rakishou.kurobanewnavstacktest.widget

import android.content.Context
import android.util.AttributeSet
import androidx.drawerlayout.widget.DrawerLayout

class DrawerWidthAdjustingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : DrawerLayout(context, attrs, defStyle) {

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
        MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.EXACTLY
    )

    val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
        MeasureSpec.getSize(heightMeasureSpec),
        MeasureSpec.EXACTLY
    )

    super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
  }

}