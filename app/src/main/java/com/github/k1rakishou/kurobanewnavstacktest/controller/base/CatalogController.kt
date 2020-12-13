package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.feature.CatalogLayout
import com.github.k1rakishou.kurobanewnavstacktest.feature.CatalogViewModel
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import kotlinx.coroutines.Job
import timber.log.Timber

abstract class CatalogController(
  args: Bundle? = null
) : BaseController(args),
  CatalogNavigationContract, CatalogLayout.CatalogControllerCallbacks {

  protected lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks
  private lateinit var catalogLayout: CatalogLayout

  protected val controllerType = ControllerType.Catalog
  private val testHelpers by lazy { (activity as MainActivity).testHelpers }
  private val catalogViewModel by lazy { viewModels(CatalogViewModel::class).value }

  private var toolbarContract: ToolbarContract? = null
  private var threadNavigationContract: ThreadNavigationContract? = null
  private var job: Job? = null

  fun uiElementsControllerCallbacks(uiElementsControllerCallbacks: UiElementsControllerCallbacks) {
    this.uiElementsControllerCallbacks = uiElementsControllerCallbacks
  }

  fun threadNavigationContract(threadNavigationContract: ThreadNavigationContract) {
    this.threadNavigationContract = threadNavigationContract
  }

  fun toolbarContract(toolbarContract: ToolbarContract) {
    this.toolbarContract = toolbarContract
  }

  final override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    catalogLayout = CatalogLayout(currentContext())
    return catalogLayout
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    checkNotNull(toolbarContract) { "toolbarContract is null" }

    catalogLayout.onCreated(
      toolbarContract!!,
      uiElementsControllerCallbacks,
      this,
      catalogViewModel,
      testHelpers
    )
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    catalogLayout.onDestroyed()
    threadNavigationContract = null

    job?.cancel()
    job = null
  }

  override fun openBoard(boardDescriptor: BoardDescriptor) {
    Timber.tag(TAG).d("openBoard($boardDescriptor)")
    catalogLayout.openBoard(boardDescriptor)
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    threadNavigationContract?.openThread(threadDescriptor)
  }

  companion object {
    private const val TAG = "CatalogController"
  }
}