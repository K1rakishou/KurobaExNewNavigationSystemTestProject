package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog

import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateUpdate

data class KurobaCatalogToolbarState(
  var slideProgress: Float? = null,
  var title: String? = null,
  var subtitle: String? = null,
  var enableControls: Boolean? = null
) : ToolbarStateContract {

  override fun updateState(newStateUpdate: ToolbarStateUpdate) {
    if (newStateUpdate !is ToolbarStateUpdate.Catalog) {
      return
    }

    when (newStateUpdate) {
      is ToolbarStateUpdate.Catalog.UpdateSlideProgress -> {
        slideProgress = newStateUpdate.slideProgress
      }
      is ToolbarStateUpdate.Catalog.UpdateTitle -> {
        title = newStateUpdate.title
      }
      is ToolbarStateUpdate.Catalog.UpdateSubTitle -> {
        subtitle = newStateUpdate.subTitle
      }
      ToolbarStateUpdate.Catalog.PreSlideProgressUpdates -> {
        enableControls = false
      }
      ToolbarStateUpdate.Catalog.PostSlideProgressUpdates -> {
        enableControls = true
      }
    }.exhaustive
  }

  override fun reset() {
    this.title = null
    this.subtitle = null
    this.enableControls = null
  }

}