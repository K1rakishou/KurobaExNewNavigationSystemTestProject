package com.github.k1rakishou.kurobanewnavstacktest.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout

class TouchBlockingCoordinatorLayout @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttr: Int = 0
) : CoordinatorLayout(context, attributeSet, defStyleAttr) {

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    return true
  }

}