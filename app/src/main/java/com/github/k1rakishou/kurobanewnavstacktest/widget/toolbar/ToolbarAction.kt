package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

sealed class ToolbarAction(val toolbarType: KurobaToolbarType) {

  sealed class Catalog(toolbarType: KurobaToolbarType) : ToolbarAction(toolbarType) {
    class BoardSelectionMenuButtonClicked(toolbarType: KurobaToolbarType) : Catalog(toolbarType)
    class RefreshCatalogButtonClicked(toolbarType: KurobaToolbarType) : Catalog(toolbarType)
    class OpenSubMenuButtonClicked(toolbarType: KurobaToolbarType) : Catalog(toolbarType)
  }

  sealed class Thread(toolbarType: KurobaToolbarType) : ToolbarAction(toolbarType) {
    class OpenGalleryButtonClicked(toolbarType: KurobaToolbarType) : Thread(toolbarType)
    class BookmarkThreadButtonClicked(toolbarType: KurobaToolbarType) : Thread(toolbarType)
    class OpenSubmenuButtonClicked(toolbarType: KurobaToolbarType) : Thread(toolbarType)
  }

  sealed class Search(toolbarType: KurobaToolbarType) : ToolbarAction(toolbarType) {
    class SearchShown(toolbarType: KurobaToolbarType) : Search(toolbarType)
    class SearchHidden(toolbarType: KurobaToolbarType) : Search(toolbarType)
    class QueryUpdated(toolbarType: KurobaToolbarType, val query: String) : Search(toolbarType)
  }
}