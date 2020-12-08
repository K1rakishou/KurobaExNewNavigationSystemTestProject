package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.lang.IllegalStateException

open class KurobaToolbarViewModel : ViewModel() {
  @OptIn(ExperimentalCoroutinesApi::class)
  private val toolbarStateChangeFlow = BroadcastChannel<ToolbarStateClass>(capacity = 128)
  private val toolbarActionFlow = MutableSharedFlow<ToolbarAction>(
    replay = 1,
    extraBufferCapacity = 128,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private var prevToolbarStateClass = ToolbarStateClass.Uninitialized

  @OptIn(ExperimentalCoroutinesApi::class)
  fun listenForToolbarStateChanges(): Flow<ToolbarStateClass> {
    return toolbarStateChangeFlow.asFlow()
  }

  fun listenForToolbarActions(predicate: (ToolbarAction) -> Boolean): Flow<ToolbarAction> {
    return toolbarActionFlow
      .distinctUntilChangedBy { toolbarAction -> toolbarAction }
      .filter { toolbarAction -> predicate(toolbarAction) }
  }

  fun fireAction(toolbarAction: ToolbarAction) {
    toolbarActionFlow.tryEmit(toolbarAction)
  }

  fun newState(newStateUpdate: ToolbarStateUpdate) {
    updateState(newStateUpdate)

    toolbarStateChangeFlow.offer(newStateUpdate.toolbarStateClass)
  }

  fun getPrevToolbarStateClass(): ToolbarStateClass {
    return prevToolbarStateClass
  }

  fun updatePrevToolbarStateClass(newToolbarStateClass: ToolbarStateClass) {
    prevToolbarStateClass = newToolbarStateClass
  }

  open fun updateState(newStateUpdate: ToolbarStateUpdate) {
    throw IllegalStateException("Must not be called")
  }

}