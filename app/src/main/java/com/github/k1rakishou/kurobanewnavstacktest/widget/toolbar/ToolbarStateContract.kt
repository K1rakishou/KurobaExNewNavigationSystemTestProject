package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

interface ToolbarStateContract {
  fun updateState(newStateUpdate: ToolbarStateUpdate)
  fun reset()
}