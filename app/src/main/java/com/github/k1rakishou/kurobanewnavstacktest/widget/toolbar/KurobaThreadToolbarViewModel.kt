package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

class KurobaThreadToolbarViewModel : KurobaToolbarViewModel() {
  private val threadToolbarState = KurobaThreadToolbarState()

  override fun updateState(newStateUpdate: ToolbarStateUpdate) {
    if (newStateUpdate !is ToolbarStateUpdate.Thread) {
      return
    }

    when (newStateUpdate) {
      is ToolbarStateUpdate.Thread.UpdateSlideProgress -> {
        threadToolbarState.slideProgress = newStateUpdate.slideProgress
      }
      is ToolbarStateUpdate.Thread.UpdateTitle -> {
        threadToolbarState.threadTitle = newStateUpdate.threadTitle
      }
      ToolbarStateUpdate.Thread.PreSlideProgressUpdates -> {
        threadToolbarState.enableControls = false
      }
      ToolbarStateUpdate.Thread.PostSlideProgressUpdates -> {
        threadToolbarState.enableControls = true
      }
    }
  }

  fun getThreadToolbarState(): KurobaThreadToolbarState = threadToolbarState

  data class KurobaThreadToolbarState(
    var slideProgress: Float? = null,
    var threadTitle: String? = null,
    var enableControls: Boolean? = null
  ) {

    fun fillFrom(other: KurobaThreadToolbarState) {
      this.slideProgress = other.slideProgress
      this.threadTitle = other.threadTitle
      this.enableControls = other.enableControls
    }

    fun reset() {
      this.slideProgress = null
      this.threadTitle = null
      this.enableControls = null
    }

  }
}