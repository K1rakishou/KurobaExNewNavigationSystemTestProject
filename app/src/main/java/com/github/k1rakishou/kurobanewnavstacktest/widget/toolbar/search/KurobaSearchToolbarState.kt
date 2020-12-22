package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search

import com.github.k1rakishou.kurobanewnavstacktest.utils.exhaustive
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarAction
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateUpdate


data class KurobaSearchToolbarState(
  var query: String? = null,
  var foundItems: FoundItems? = null
) : ToolbarStateContract {

  fun updateQuery(newQuery: String?) {
    this.query = newQuery
  }

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

  override fun restoreLastToolbarActions(toolbarType: KurobaToolbarType): List<ToolbarAction> {
    val stateList = mutableListOf<ToolbarAction>()
    val searchQuery = query

    stateList += ToolbarAction.Search.SearchShown(toolbarType)

    if (searchQuery != null && searchQuery.isNotEmpty()) {
      stateList += ToolbarAction.Search.QueryUpdated(toolbarType, searchQuery)
    }

    return stateList
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