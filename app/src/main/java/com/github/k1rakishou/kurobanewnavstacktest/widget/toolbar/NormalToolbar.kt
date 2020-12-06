package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndRequest
import com.google.android.material.appbar.MaterialToolbar

class NormalToolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : FrameLayout(context, attributeSet, attrDefStyle), ToolbarContract {
  private val actualToolbar: MaterialToolbar

  init {
    inflate(context, R.layout.widget_normal_toolbar, this)
    val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()

    val normalToolbarRoot = findViewById<FrameLayout>(R.id.normal_toolbar_root)
    actualToolbar = findViewById(R.id.normal_toolbar)

    normalToolbarRoot.setOnApplyWindowInsetsListenerAndRequest { v, insets ->
      v.updateLayoutParams<FrameLayout.LayoutParams> {
        height = toolbarHeight + insets.systemWindowInsetTop
      }
      v.updatePadding(top = insets.systemWindowInsetTop)

      return@setOnApplyWindowInsetsListenerAndRequest insets
    }
  }

  override fun collapsableView(): View {
    return this
  }

  override fun setTitle(title: String) {
    actualToolbar.title = title
  }

}