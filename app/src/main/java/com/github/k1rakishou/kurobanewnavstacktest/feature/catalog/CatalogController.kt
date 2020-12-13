package com.github.k1rakishou.kurobanewnavstacktest.feature.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CatalogNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.ThreadNavigationContract
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

abstract class CatalogController(
  args: Bundle? = null
) : BaseController(args),
  CatalogNavigationContract, CatalogLayout.CatalogControllerCallbacks {

  protected lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks
  private lateinit var toolbarContract: ToolbarContract
  private lateinit var threadNavigationContract: ThreadNavigationContract
  private lateinit var catalogLayout: CatalogLayout

  protected val controllerType = ControllerType.Catalog
  private val testHelpers by lazy { (activity as MainActivity).testHelpers }
  private val catalogViewModel by lazy { viewModels(CatalogViewModel::class).value }

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

    catalogLayout.onCreate(
      toolbarContract,
      uiElementsControllerCallbacks,
      this,
      catalogViewModel,
      testHelpers
    )
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    catalogLayout.onDestroy()
  }

  override fun handleBack(): Boolean {
    if (catalogLayout.onBackPressed()) {
      return true
    }

    return super.handleBack()
  }

  override fun openBoard(boardDescriptor: BoardDescriptor) {
    catalogLayout.openBoard(boardDescriptor)
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    threadNavigationContract.openThread(threadDescriptor)
  }

  companion object {
    private const val TAG = "CatalogController"
  }
}