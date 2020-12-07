package com.github.k1rakishou.kurobanewnavstacktest.widget

import android.content.Context
import android.util.AttributeSet
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp
import com.google.android.material.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class KurobaFloatingActionButton @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defAttrStyle: Int = R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attributeSet, defAttrStyle) {

  companion object {
    val DEFAULT_MARGIN_RIGHT = 16.dp
  }
}