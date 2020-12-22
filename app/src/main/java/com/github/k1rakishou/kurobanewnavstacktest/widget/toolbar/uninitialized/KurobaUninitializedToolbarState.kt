package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.uninitialized

import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarAction
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateUpdate

class KurobaUninitializedToolbarState : ToolbarStateContract {

  override fun updateState(newStateUpdate: ToolbarStateUpdate) {
    // no-op
  }

  override fun restoreLastToolbarActions(toolbarType: KurobaToolbarType): List<ToolbarAction> {
    return emptyList()
  }

  override fun reset() {
    // no-op
  }

}