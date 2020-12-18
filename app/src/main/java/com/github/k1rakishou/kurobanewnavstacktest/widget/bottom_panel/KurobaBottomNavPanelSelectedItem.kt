package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

enum class KurobaBottomNavPanelSelectedItem(val value: Int) {
  Uninitialized(-1),
  Search(0),
  Bookmarks(1),
  Browse(2),
  Settings(3);

  companion object {
    fun fromInt(value: Int?): KurobaBottomNavPanelSelectedItem {
      if (value == null) {
        return Uninitialized
      }

      return values().firstOrNull { selectedItem -> selectedItem.value == value }
        ?: Uninitialized
    }
  }
}