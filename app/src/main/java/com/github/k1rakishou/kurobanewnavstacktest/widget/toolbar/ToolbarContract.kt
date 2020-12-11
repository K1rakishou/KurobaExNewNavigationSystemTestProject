package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import android.view.View
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

interface ToolbarContract {
  fun collapsableView(): View
  fun setToolbarVisibility(visibility: Int)
  fun onBackPressed(): Boolean

  fun showDefaultToolbar(toolbarType: KurobaToolbarType)

  fun showSearchToolbar(toolbarType: KurobaToolbarType)
  fun closeSearchToolbar(toolbarType: KurobaToolbarType)

  fun setTitle(toolbarType: KurobaToolbarType, title: String)
  fun setSubTitle(subtitle: String)
}