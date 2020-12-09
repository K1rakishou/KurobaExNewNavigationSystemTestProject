package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

interface KurobaToolbarCallbacks {
  fun popCurrentToolbarStateClass()
  fun pushNewToolbarStateClass(toolbarStateClass: ToolbarStateClass)
}