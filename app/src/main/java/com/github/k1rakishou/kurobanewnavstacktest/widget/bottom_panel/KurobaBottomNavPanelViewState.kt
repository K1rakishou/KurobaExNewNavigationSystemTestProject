package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

data class KurobaBottomNavPanelViewState(
  var selectedItem: KurobaBottomNavPanelSelectedItem = KurobaBottomNavPanelSelectedItem.Uninitialized
) {

  fun isNotDefaultState(): Boolean {
    return selectedItem != KurobaBottomNavPanelSelectedItem.Uninitialized
  }

  fun fillFromOther(other: KurobaBottomNavPanelViewState?) {
    if (other == null) {
      return
    }

    this.selectedItem = other.selectedItem
  }
}