package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.google.android.material.appbar.MaterialToolbar

class Toolbar @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  attrDefStyle: Int = 0
) : FrameLayout(context, attributeSet, attrDefStyle), ToolbarContract {
  private val actualToolbar: MaterialToolbar

  init {
    inflate(context, R.layout.widget_catalog_toolbar, this)

    actualToolbar = findViewById(R.id.toolbar)
  }

  override fun collapsableView(): View {
    return this
  }

  override fun setTitle(title: String) {
    actualToolbar.title = title
  }

}