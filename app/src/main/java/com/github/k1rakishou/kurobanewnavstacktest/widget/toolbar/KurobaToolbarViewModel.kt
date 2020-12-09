package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import androidx.lifecycle.ViewModel
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog.KurobaCatalogToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search.KurobaSearchToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread.KurobaThreadToolbarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

class KurobaToolbarViewModel : ViewModel() {
  private val stateMap = mutableMapOf<ToolbarStateClass, ToolbarStateContract>()

  @OptIn(ExperimentalCoroutinesApi::class)
  private val toolbarStateChangeFlow = BroadcastChannel<ToolbarStateClass>(capacity = 128)
  @OptIn(ExperimentalCoroutinesApi::class)
  private val toolbarActionFlow = BroadcastChannel<ToolbarAction>(capacity = 128)

  private val toolbarStateStackMap = mutableMapOf<ToolbarStateClass, KurobaToolbarStateStack>()

  init {
    stateMap[ToolbarStateClass.Thread] = KurobaThreadToolbarState()
    stateMap[ToolbarStateClass.Catalog] = KurobaCatalogToolbarState()
    stateMap[ToolbarStateClass.Search] = KurobaSearchToolbarState()
  }

  fun initStateStackForToolbar(toolbarStateClass: ToolbarStateClass) {
    if (toolbarStateStackMap.containsKey(toolbarStateClass)) {
      return
    }

    toolbarStateStackMap.put(toolbarStateClass, KurobaToolbarStateStack())
  }

  fun getToolbarStateStack(toolbarStateClass: ToolbarStateClass): KurobaToolbarStateStack {
    return requireNotNull(toolbarStateStackMap[toolbarStateClass]) {
      "KurobaToolbarStateStack not initialized for toolbarStateClass=$toolbarStateClass"
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  fun listenForToolbarStateChanges(): Flow<ToolbarStateClass> {
    return toolbarStateChangeFlow.asFlow()
  }

  fun listenForToolbarActions(predicate: (ToolbarAction) -> Boolean): Flow<ToolbarAction> {
    return toolbarActionFlow
      .asFlow()
      .distinctUntilChangedBy { toolbarAction -> toolbarAction }
      .filter { toolbarAction -> predicate(toolbarAction) }
  }

  fun fireAction(toolbarAction: ToolbarAction) {
    toolbarActionFlow.offer(toolbarAction)
  }

  fun newState(newStateUpdate: ToolbarStateUpdate) {
    val toolbarStateClass = newStateUpdate.toolbarStateClass

    val toolbarStateContract = requireNotNull(stateMap[toolbarStateClass]) {
      "State was not initialized for toolbarStateClass=$toolbarStateClass"
    }

    toolbarStateContract.updateState(newStateUpdate)
    toolbarStateChangeFlow.offer(newStateUpdate.toolbarStateClass)
  }

  fun resetState(toolbarStateClass: ToolbarStateClass) {
    val toolbarStateContract = requireNotNull(stateMap[toolbarStateClass]) {
      "State was not initialized for toolbarStateClass=$toolbarStateClass"
    }

    toolbarStateContract.reset()
  }

  fun <T : ToolbarStateContract> getToolbarState(toolbarStateClass: ToolbarStateClass): T {
    return requireNotNull(stateMap[toolbarStateClass]) {
      "State was not initialized for toolbarStateClass=$toolbarStateClass"
    } as T
  }

}