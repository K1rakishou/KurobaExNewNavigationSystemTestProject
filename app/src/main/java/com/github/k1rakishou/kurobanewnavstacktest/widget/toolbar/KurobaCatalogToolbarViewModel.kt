package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

class KurobaCatalogToolbarViewModel : KurobaToolbarViewModel() {
  private val catalogToolbarState = KurobaCatalogToolbarState()

  override fun updateState(newStateUpdate: ToolbarStateUpdate) {
    if (newStateUpdate !is ToolbarStateUpdate.Catalog) {
      return
    }

    when (newStateUpdate) {
      is ToolbarStateUpdate.Catalog.UpdateSlideProgress -> {
        catalogToolbarState.slideProgress = newStateUpdate.slideProgress
      }
      is ToolbarStateUpdate.Catalog.UpdateTitle -> {
        catalogToolbarState.title = newStateUpdate.title
      }
      is ToolbarStateUpdate.Catalog.UpdateSubTitle -> {
        catalogToolbarState.subtitle = newStateUpdate.subTitle
      }
      ToolbarStateUpdate.Catalog.PreSlideProgressUpdates -> {
        catalogToolbarState.enableControls = false
      }
      ToolbarStateUpdate.Catalog.PostSlideProgressUpdates -> {
        catalogToolbarState.enableControls = true
      }
    }
  }

  fun getCatalogToolbarState(): KurobaCatalogToolbarState = catalogToolbarState

  data class KurobaCatalogToolbarState(
    var slideProgress: Float? = null,
    var title: String? = null,
    var subtitle: String? = null,
    var enableControls: Boolean? = null
  ) {

    fun fillFrom(other: KurobaCatalogToolbarState) {
      this.slideProgress = other.slideProgress
      this.title = other.title
      this.subtitle = other.subtitle
      this.enableControls = other.enableControls
    }

    fun reset() {
      this.slideProgress = null
      this.title = null
      this.subtitle = null
      this.enableControls = null
    }

  }
}