package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import kotlinx.coroutines.flow.Flow

interface ToolbarContract : CollapsableView {
  fun collapsableView(): CollapsableView
  fun setToolbarVisibility(visibility: Int)
  fun onBackPressed(): Boolean

  fun showDefaultToolbar(toolbarType: KurobaToolbarType)
  fun showSearchToolbar(toolbarType: KurobaToolbarType)
  fun closeSearchToolbar(toolbarType: KurobaToolbarType)
  fun closeToolbar(toolbarType: KurobaToolbarType)

  fun listenForToolbarActions(toolbarType: KurobaToolbarType): Flow<ToolbarAction>

  fun setTitle(toolbarType: KurobaToolbarType, title: String)
  fun setSubTitle(subtitle: String)
}