package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.widget.SlidingPaneLayoutEx
import com.github.k1rakishou.kurobanewnavstacktest.widget.SlidingPaneLayoutSlideHandler

class SlideNavController(args: Bundle? = null) : BaseController(args) {
  private lateinit var slidingPaneLayout: SlidingPaneLayoutEx
  private lateinit var catalogControllerContainer: FrameLayout
  private lateinit var threadControllerContainer: FrameLayout
  private lateinit var slidingPaneLayoutSlideHandler: SlidingPaneLayoutSlideHandler

  private var uiElementsControllerCallbacks: UiElementsControllerCallbacks? = null
  private var slideCatalogUiElementsControllerCallbacks: SlideCatalogUiElementsControllerCallbacks? = null

  fun setUiElementsControllerCallbacks(callbacks: UiElementsControllerCallbacks) {
    uiElementsControllerCallbacks = callbacks
  }

  fun setSlideCatalogUiElementsControllerCallbacks(callbacks: SlideCatalogUiElementsControllerCallbacks) {
    slideCatalogUiElementsControllerCallbacks = callbacks
  }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_slide_navigation, container) {
      catalogControllerContainer = findViewById(R.id.catalog_controller_container)
      threadControllerContainer = findViewById(R.id.thread_controller_container)
      slidingPaneLayout = findViewById(R.id.sliding_pane_layout)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    catalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(SlideCatalogController())
    )

    threadControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(SlideThreadController())
    )

    slidingPaneLayout.setSlidingPaneLayoutDefaultState()

    slidingPaneLayoutSlideHandler = SlidingPaneLayoutSlideHandler(true).apply {
      addListener(object : SlidingPaneLayoutSlideHandler.SlidingPaneLayoutSlideListener {
        override fun onSlidingStarted(wasOpen: Boolean) {
          // TODO(KurobaEx):
//          homeFabViewControllerCallbacks?.onSlidingPaneSlidingStarted(wasOpen)
        }

        override fun onSliding(offset: Float) {
          // TODO(KurobaEx):
//          homeControllerCallbacks?.onSliding(offset)
//          homeFabViewControllerCallbacks?.onSlidingPaneSliding(offset)

          slideCatalogUiElementsControllerCallbacks?.onSliding(offset)
        }

        override fun onSlidingEnded(becameOpen: Boolean) {
          // TODO(KurobaEx):
//          homeFabViewControllerCallbacks?.onSlidingPaneSlidingEnded(becameOpen)

          fireSlidingPaneListeners(becameOpen)
        }
      })

      slidingPaneLayout.setPanelSlideListener(this)
    }

    slidingPaneLayout.open()
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    uiElementsControllerCallbacks = null
    slideCatalogUiElementsControllerCallbacks = null
  }

  private fun SlidingPaneLayoutEx.setSlidingPaneLayoutDefaultState() {
    open()
    fireSlidingPaneListeners(isOpen)
  }

  private fun fireSlidingPaneListeners(open: Boolean) {
    if (open) {
      // TODO(KurobaEx):
//      homeControllerCallbacks?.onControllerLostFocus(isCatalogController = false)
      getThreadControllerOrNull()?.onLostFocus()

      // TODO(KurobaEx):
//      homeControllerCallbacks?.onControllerGainedFocus(isCatalogController = true)
      getCatalogControllerOrNull()?.onGainedFocus()

      slideCatalogUiElementsControllerCallbacks?.onControllerGainedFocus(isCatalogController = true)
    } else {
      // TODO(KurobaEx):
//      homeControllerCallbacks?.onControllerLostFocus(isCatalogController = true)
      getCatalogControllerOrNull()?.onLostFocus()

      // TODO(KurobaEx):
//      homeControllerCallbacks?.onControllerGainedFocus(isCatalogController = false)
      getThreadControllerOrNull()?.onGainedFocus()

      slideCatalogUiElementsControllerCallbacks?.onControllerGainedFocus(isCatalogController = false)
    }
  }

  private fun getThreadControllerOrNull(): SlideThreadController? {
    return threadControllerContainer.getControllerByTag(SlideThreadController.CONTROLLER_TAG)
  }

  private fun getCatalogControllerOrNull(): SlideCatalogController? {
    return threadControllerContainer.getControllerByTag(SlideCatalogController.CONTROLLER_TAG)
  }

  private fun isCatalogControllerFocused(): Boolean {
    return slidingPaneLayout.isOpen
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  interface SlideCatalogUiElementsControllerCallbacks {
    fun onSliding(offset: Float)
    fun onControllerGainedFocus(isCatalogController: Boolean)
  }

  companion object {
    val CONTROLLER_TAG = ControllerTag("SlideNavControllerTag")
  }
}