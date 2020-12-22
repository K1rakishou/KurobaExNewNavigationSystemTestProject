package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread

import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarAction
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateUpdate

data class KurobaThreadToolbarState(
  var slideProgress: Float? = null,
  var threadTitle: String? = null,
  var enableControls: Boolean? = null
) : ToolbarStateContract {

  override fun updateState(newStateUpdate: ToolbarStateUpdate) {
    if (newStateUpdate !is ToolbarStateUpdate.Thread) {
      return
    }

    when (newStateUpdate) {
      is ToolbarStateUpdate.Thread.UpdateSlideProgress -> {
        slideProgress = newStateUpdate.slideProgress
      }
      is ToolbarStateUpdate.Thread.UpdateTitle -> {
        threadTitle = newStateUpdate.threadTitle
      }
      ToolbarStateUpdate.Thread.PreSlideProgressUpdates -> {
        enableControls = false
      }
      ToolbarStateUpdate.Thread.PostSlideProgressUpdates -> {
        enableControls = true
      }
    }.exhaustive
  }

  override fun restoreLastToolbarActions(toolbarType: KurobaToolbarType): List<ToolbarAction> {
    return emptyList()
  }

  override fun reset() {
    this.threadTitle = null
    this.enableControls = null
  }

}