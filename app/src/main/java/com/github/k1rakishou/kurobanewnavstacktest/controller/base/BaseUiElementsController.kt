package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.BookmarksController
import com.github.k1rakishou.kurobanewnavstacktest.controller.SettingsController
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.NormalToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.SlideToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

abstract class BaseUiElementsController(
  args: Bundle? = null
) : BaseController(args) {
  protected lateinit var toolbarContract: ToolbarContract

  protected lateinit var bottomPanel: KurobaBottomPanel
  protected lateinit var toolbarContainer: FrameLayout

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    setupToolbar()
  }

  private fun setupToolbar() {
    if (isSplitLayout()) {
      val toolbarView = NormalToolbar(currentContext())
      toolbarView.init(KurobaToolbarType.Catalog)
      toolbarContainer.addView(toolbarView)

      toolbarContract = toolbarView
    } else {
      val toolbarView = SlideToolbar(currentContext())
      toolbarView.init()
      toolbarContainer.addView(toolbarView)

      toolbarContract = toolbarView
    }
  }

  override fun handleBack(): Boolean {
    if (::toolbarContract.isInitialized && toolbarContract.onBackPressed()) {
      return true
    }

    return super.handleBack()
  }

  protected fun ViewGroup.switchTo(controller: BaseController) {
    val router = getChildRouter(this)

    val topControllerTag = (router.getTopController() as? BaseController)?.getControllerTag()
    if (topControllerTag == controller.getControllerTag()) {
      // This controller is already the topmost
      return
    }

    router.replaceTopController(
        RouterTransaction.with(controller)
          .tag(controller.getControllerTag().tag)
          .pushChangeHandler(FadeChangeHandler())
          .popChangeHandler(FadeChangeHandler())
      )
  }

  protected fun createSettingsController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): SettingsController {
    return SettingsController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
      toolbarContract(toolbarContract)
    }
  }

  protected fun createBookmarksController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): BookmarksController {
    return BookmarksController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
      toolbarContract(toolbarContract)
    }
  }

  protected abstract fun isSplitLayout(): Boolean

  companion object {
    val CONTROLLER_TAG = ControllerTag("CatalogUiElementsControllerTag")
  }
}