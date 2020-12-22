package com.github.k1rakishou.kurobanewnavstacktest.feature.thread

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.viewmodel.MainControllerViewModel
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

abstract class ThreadController(
  args: Bundle? = null
) : BaseController(args),
  ThreadNavigationContract, ThreadLayout.ThreadControllerCallbacks {
  protected lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks
  protected lateinit var toolbarContract: ToolbarContract
  private lateinit var threadLayout: ThreadLayout

  protected val controllerType = ControllerType.Thread
  private val testHelpers by lazy { (activity as MainActivity).testHelpers }

  private val threadViewModel by viewModels(ThreadViewModel::class)
  private val mainControllerViewModel by viewModels(MainControllerViewModel::class)

  protected val isToolbarContractInitialized: Boolean
    get() = ::toolbarContract.isInitialized

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  fun provideToolbarContract(toolbarContract: ToolbarContract) {
    this.toolbarContract = toolbarContract
  }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    threadLayout = ThreadLayout(currentContext())
    return threadLayout
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    threadLayout.onCreate(
      toolbarContract = toolbarContract,
      uiElementsControllerCallbacks = uiElementsControllerCallbacks,
      threadControllerCallbacks = this,
      threadViewModel = threadViewModel,
      mainControllerViewModel = mainControllerViewModel,
      testHelpers = testHelpers
    )
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    threadLayout.onDestroy()
  }

  override fun myHandleBack(): Boolean {
    if (threadLayout.onBackPressed()) {
      return true
    }

    return false
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    threadLayout.openThread(threadDescriptor)
  }

  companion object {
    private const val TAG = "ThreadController"
  }
}