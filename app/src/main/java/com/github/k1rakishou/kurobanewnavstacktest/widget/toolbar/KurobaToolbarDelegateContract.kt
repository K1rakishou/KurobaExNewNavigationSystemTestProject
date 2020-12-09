package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

interface KurobaToolbarDelegateContract<T : ToolbarStateContract> {
  val toolbarStateClass: ToolbarStateClass

  fun applyStateToUi(toolbarState: T)
}