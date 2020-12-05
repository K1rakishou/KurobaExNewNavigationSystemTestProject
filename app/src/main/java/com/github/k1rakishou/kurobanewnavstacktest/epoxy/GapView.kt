package com.github.k1rakishou.kurobanewnavstacktest.epoxy

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Space
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.github.k1rakishou.kurobanewnavstacktest.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class GapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
  private val gapView: Space

  init {
    inflate(context, R.layout.epoxy_gap_view, this)

    gapView = findViewById(R.id.gap_view)
  }

  @ModelProp
  fun setHeight(newHeight: Int) {
    gapView.updateLayoutParams<LayoutParams> { height = newHeight }
  }

}