package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.BookmarksController
import com.github.k1rakishou.kurobanewnavstacktest.controller.SettingsController
import com.github.k1rakishou.kurobanewnavstacktest.controller.slide.SlideNavController
import com.github.k1rakishou.kurobanewnavstacktest.controller.split.SplitCatalogController
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndRequest
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.SlideToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.NormalToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class CatalogUiElementsController(args: Bundle? = null) : BaseController(args) {
  protected lateinit var toolbarContract: ToolbarContract

  protected lateinit var bottomNavView: BottomNavigationView
  protected lateinit var toolbarContainer: FrameLayout

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    setupBottomNavView()
    setupToolbar()
  }

  private fun setupToolbar() {
    if (isSplitLayout()) {
      val toolbarView = NormalToolbar(currentContext())
      toolbarContainer.addView(toolbarView)

      toolbarContract = toolbarView
    } else {
      val toolbarView = SlideToolbar(currentContext())
      toolbarContainer.addView(toolbarView)

      toolbarContract = toolbarView
    }
  }

  private fun setupBottomNavView() {
    val bottomNavViewHeight =
      currentContext().resources.getDimension(R.dimen.bottom_nav_view_height).toInt()

    bottomNavView.setOnApplyWindowInsetsListenerAndRequest { v, insets ->
      v.updateLayoutParams<CoordinatorLayout.LayoutParams> {
        height = bottomNavViewHeight + insets.systemWindowInsetBottom
      }
      v.updatePadding(bottom = insets.systemWindowInsetBottom)

      return@setOnApplyWindowInsetsListenerAndRequest insets
    }
  }

  protected fun ViewGroup.switchTo(controller: BaseController) {
    getChildRouter(this).replaceTopController(
      RouterTransaction.with(controller)
        .tag(controller.getControllerTag().tag)
        .pushChangeHandler(FadeChangeHandler())
        .popChangeHandler(FadeChangeHandler())
    )
  }

  protected fun createSlideNavController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks,
    slideCatalogUiElementsControllerCallbacks: SlideNavController.SlideCatalogUiElementsControllerCallbacks
  ): SlideNavController {
    return SlideNavController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
      setSlideCatalogUiElementsControllerCallbacks(slideCatalogUiElementsControllerCallbacks)
    }
  }

  protected fun createSplitCatalogController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): SplitCatalogController {
    return SplitCatalogController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
    }
  }

  protected fun createSettingsController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): SettingsController {
    return SettingsController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
    }
  }

  protected fun createBookmarksController(
    uiElementsControllerCallbacks: UiElementsControllerCallbacks
  ): BookmarksController {
    return BookmarksController().apply {
      setUiElementsControllerCallbacks(uiElementsControllerCallbacks)
    }
  }

  protected abstract fun isSplitLayout(): Boolean

  companion object {
    val CONTROLLER_TAG = ControllerTag("CatalogUiElementsControllerTag")
  }
}