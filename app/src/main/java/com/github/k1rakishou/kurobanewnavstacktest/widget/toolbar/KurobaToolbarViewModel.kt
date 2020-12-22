package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import androidx.lifecycle.ViewModel
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.catalog.KurobaCatalogToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search.KurobaSearchToolbarState
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.thread.KurobaThreadToolbarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.lang.IllegalStateException

class KurobaToolbarViewModel : ViewModel() {
  private val stateMap = mutableMapOf<KurobaToolbarType, MutableMap<ToolbarStateClass, ToolbarStateContract>>()
  private val toolbarStateStackMap = mutableMapOf<KurobaToolbarType, KurobaToolbarStateStack>()

  private val toolbarStateChangeFlow =  MutableSharedFlow<NewToolbarStateUpdate>(
    replay = 1,
    extraBufferCapacity = 1024
  )

  private val toolbarActionFlow = MutableSharedFlow<ToolbarAction>(
    replay = 1,
    extraBufferCapacity = 1024
  )

  fun initStateStackForToolbar(kurobaToolbarType: KurobaToolbarType) {
    if (toolbarStateStackMap.containsKey(kurobaToolbarType)) {
      return
    }

    toolbarStateStackMap.put(kurobaToolbarType, KurobaToolbarStateStack(kurobaToolbarType))
  }

  fun getToolbarStateStack(kurobaToolbarType: KurobaToolbarType): KurobaToolbarStateStack {
    return requireNotNull(toolbarStateStackMap[kurobaToolbarType]) {
      "KurobaToolbarStateStack not initialized for kurobaToolbarType=$kurobaToolbarType"
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  fun listenForToolbarStateChanges(): Flow<NewToolbarStateUpdate> {
    return toolbarStateChangeFlow.asSharedFlow()
  }

  fun listenForToolbarActions(toolbarType: KurobaToolbarType): Flow<ToolbarAction> {
    return toolbarActionFlow
      .asSharedFlow()
      .filter { toolbarAction -> toolbarAction.toolbarType == toolbarType }
      .distinctUntilChangedBy { toolbarAction -> toolbarAction }
  }

  fun fireAction(toolbarAction: ToolbarAction) {
    toolbarActionFlow.tryEmit(toolbarAction)
  }

  fun newState(toolbarType: KurobaToolbarType, newStateUpdate: ToolbarStateUpdate) {
    val toolbarStateClass = newStateUpdate.toolbarStateClass
    createDefaultState(toolbarType, toolbarStateClass)

    val toolbarStateContract = stateMap[toolbarType]!![toolbarStateClass]!!
    toolbarStateContract.updateState(newStateUpdate)

    val newToolbarStateUpdate = NewToolbarStateUpdate(
      toolbarType,
      newStateUpdate.toolbarStateClass
    )

    toolbarStateChangeFlow.tryEmit(newToolbarStateUpdate)
  }

  fun resetState(toolbarType: KurobaToolbarType, toolbarStateClass: ToolbarStateClass) {
    val toolbarStateContract = requireNotNull(stateMap[toolbarType]?.get(toolbarStateClass)) {
      "State was not initialized for toolbarType=$toolbarType and toolbarStateClass=$toolbarStateClass"
    }

    toolbarStateContract.reset()
  }

  fun <T : ToolbarStateContract> getToolbarState(
    toolbarType: KurobaToolbarType,
    toolbarStateClass: ToolbarStateClass
  ): T {
    createDefaultState(toolbarType, toolbarStateClass)

    return requireNotNull(stateMap[toolbarType]?.get(toolbarStateClass)) {
      "State was not initialized for toolbarType=$toolbarType and toolbarStateClass=$toolbarStateClass"
    } as T
  }

  private fun createDefaultState(toolbarType: KurobaToolbarType, toolbarStateClass: ToolbarStateClass) {
    var innerMap = stateMap[toolbarType]
    if (innerMap == null) {
      innerMap = mutableMapOf()
      stateMap[toolbarType] = innerMap
    }

    var toolbarStateContract = stateMap[toolbarType]!![toolbarStateClass]
    if (toolbarStateContract == null) {
      toolbarStateContract = createDefaultStateForStateClass(toolbarStateClass)
      stateMap[toolbarType]!![toolbarStateClass] = toolbarStateContract
    }
  }

  private fun createDefaultStateForStateClass(toolbarStateClass: ToolbarStateClass): ToolbarStateContract {
    return when (toolbarStateClass) {
      ToolbarStateClass.Thread -> KurobaThreadToolbarState()
      ToolbarStateClass.Catalog -> KurobaCatalogToolbarState()
      ToolbarStateClass.Search -> KurobaSearchToolbarState()
      ToolbarStateClass.SimpleTitle -> TODO()
      ToolbarStateClass.Selection -> TODO()
      ToolbarStateClass.Uninitialized -> throw IllegalStateException("Must not be called")
    }
  }

  data class NewToolbarStateUpdate(
    val kurobaToolbarType: KurobaToolbarType,
    val toolbarStateClass: ToolbarStateClass
  )

}