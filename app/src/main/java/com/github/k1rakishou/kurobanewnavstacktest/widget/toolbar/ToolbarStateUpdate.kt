package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

sealed class ToolbarStateUpdate(val toolbarStateClass: ToolbarStateClass) {
  object Uninitialized : ToolbarStateUpdate(ToolbarStateClass.Uninitialized) {
    override fun toString(): String = "Uninitialized"
  }

  sealed class Catalog : ToolbarStateUpdate(ToolbarStateClass.Catalog) {
    object PreSlideProgressUpdates : Catalog()
    data class UpdateSlideProgress(val slideProgress: Float) : Catalog()
    object PostSlideProgressUpdates : Catalog()

    data class UpdateTitle(val title: String) : Catalog()
    data class UpdateSubTitle(val subTitle: String) : Catalog()
  }

  sealed class Thread : ToolbarStateUpdate(ToolbarStateClass.Thread) {
    object PreSlideProgressUpdates : Thread()
    data class UpdateSlideProgress(val slideProgress: Float) : Thread()
    object PostSlideProgressUpdates : Thread()

    data class UpdateTitle(val threadTitle: String) : Thread()
  }

  class SimpleTitle(val title: String) : ToolbarStateUpdate(ToolbarStateClass.SimpleTitle)

  class Search(val query: String) : ToolbarStateUpdate(ToolbarStateClass.Search)

  class Selection : ToolbarStateUpdate(ToolbarStateClass.Selection)
}

enum class ToolbarStateClass {
  Uninitialized,
  Catalog,
  Thread,
  SimpleTitle,
  Search,
  Selection
}