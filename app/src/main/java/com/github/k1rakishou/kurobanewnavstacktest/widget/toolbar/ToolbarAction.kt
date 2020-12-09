package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

sealed class ToolbarAction {
  sealed class Catalog : ToolbarAction() {
    object BoardSelectionMenuButtonClicked : Catalog()
    object RefreshCatalogButtonClicked : Catalog()
    object OpenSubMenuButtonClicked : Catalog()
  }

  sealed class Thread : ToolbarAction() {
    object OpenGalleryButtonClicked : Thread()
    object BookmarkThreadButtonClicked : Thread()
    object OpenSubmenuButtonClicked : Thread()
  }

  sealed class Search : ToolbarAction() {
    class QueryUpdated(val query: String?) : Search()
  }
}