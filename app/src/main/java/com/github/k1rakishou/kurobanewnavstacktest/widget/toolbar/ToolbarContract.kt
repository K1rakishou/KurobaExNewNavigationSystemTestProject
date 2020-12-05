package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.view.View

interface ToolbarContract {
  fun collapsableView(): View
  fun setTitle(title: String)
}