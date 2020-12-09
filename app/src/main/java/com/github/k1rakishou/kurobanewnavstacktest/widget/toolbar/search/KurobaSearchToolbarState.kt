package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search

import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateUpdate


data class KurobaSearchToolbarState(
  var query: String? = null,
  var foundItems: FoundItems? = null
) : ToolbarStateContract {

  override fun updateState(newStateUpdate: ToolbarStateUpdate) {
    if (newStateUpdate !is ToolbarStateUpdate.Search) {
      return
    }

    when (newStateUpdate) {
      is ToolbarStateUpdate.Search.Query -> {
        query = newStateUpdate.query
      }
      is ToolbarStateUpdate.Search.FoundItems -> {
        foundItems = FoundItems(
          currentItemIndex = newStateUpdate.currentItemIndex,
          items = newStateUpdate.items.toList()
        )
      }
    }.exhaustive
  }

  fun updateQuery(newQuery: String?) {
    this.query = newQuery
  }

  override fun reset() {
    this.query = null
    this.foundItems = null
  }

}

data class FoundItems(
  var currentItemIndex: Int,
  var items: List<Any>
)