package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

sealed class ToolbarAction {
  sealed class Catalog : ToolbarAction() {
    object BoardSelectionMenuButtonClicked : Catalog()
    object OpenSearchButtonClicked : Catalog()
    object RefreshCatalogButtonClicked : Catalog()
    object OpenSubMenuButtonClicked : Catalog()
  }

  sealed class Thread : ToolbarAction() {
    object OpenGalleryButtonClicked : Thread()
    object BookmarkThreadButtonClicked : Thread()
    object OpenSubmenuButtonClicked : Thread()
  }
}