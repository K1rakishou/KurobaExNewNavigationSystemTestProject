package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

interface ChildPanelContract {
  fun getCurrentHeight(): Int
  fun getBackgroundColor(): Int
  fun enableOrDisableControls(enable: Boolean)
}