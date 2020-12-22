package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.*
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import com.github.k1rakishou.kurobanewnavstacktest.utils.ScreenOrientationUtils
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.SlideModeFabViewControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.getBoardDescriptorOrNull
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.getThreadDescriptorOrNull
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.putBoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.putThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.SlidingPaneLayoutEx
import com.github.k1rakishou.kurobanewnavstacktest.widget.layout.SlidingPaneLayoutSlideHandler
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

class SlideNavController(
  args: Bundle? = null
) : BaseController(args),
  RecyclerViewProvider,
  ChanNavigationContract {
  private lateinit var slidingPaneLayout: SlidingPaneLayoutEx
  private lateinit var catalogControllerContainer: FrameLayout
  private lateinit var threadControllerContainer: FrameLayout
  private lateinit var slidingPaneLayoutSlideHandler: SlidingPaneLayoutSlideHandler

  private lateinit var toolbarContract: ToolbarContract
  private lateinit var uiElementsControllerCallbacks: UiElementsControllerCallbacks
  private lateinit var slideCatalogUiElementsControllerCallbacks: SlideCatalogUiElementsControllerCallbacks
  private lateinit var recyclerViewProvider: RecyclerViewProvider
  private lateinit var slideModeFabViewControllerCallbacks: SlideModeFabViewControllerCallbacks

  private val slidingPaneLayoutSlideListener = object : SlidingPaneLayoutSlideHandler.SlidingPaneLayoutSlideListener {
    override fun onSlidingStarted(wasOpen: Boolean) {
      // Disable orientation change when sliding is in progress
      ScreenOrientationUtils.lockScreenOrientation(activityContract().activity())

      slideModeFabViewControllerCallbacks.onSlidingPaneSlidingStarted(wasOpen)
      slideCatalogUiElementsControllerCallbacks.onBeforeSliding(wasOpen.not())
    }

    override fun onSliding(offset: Float) {
      slideModeFabViewControllerCallbacks.onSlidingPaneSliding(offset)
      slideCatalogUiElementsControllerCallbacks.onSliding(offset)
    }

    override fun onSlidingEnded(becameOpen: Boolean) {
      slideCatalogUiElementsControllerCallbacks.onAfterSliding(becameOpen)

      slideModeFabViewControllerCallbacks.onSlidingPaneSlidingEnded(becameOpen)
      fireSlidingPaneListeners(becameOpen)

      // Enable orientation change back after sliding is done
      ScreenOrientationUtils.unlockScreenOrientation(activityContract().activity())
    }
  }

  fun setUiElementsControllerCallbacks(callbacks: UiElementsControllerCallbacks) {
    uiElementsControllerCallbacks = callbacks
  }

  fun setSlideCatalogUiElementsControllerCallbacks(callbacks: SlideCatalogUiElementsControllerCallbacks) {
    slideCatalogUiElementsControllerCallbacks = callbacks
  }

  fun recyclerViewProvider(recyclerViewProvider: RecyclerViewProvider) {
    this.recyclerViewProvider = recyclerViewProvider
  }

  fun slideModeFabViewControllerCallbacks(slideModeFabViewControllerCallbacks: SlideModeFabViewControllerCallbacks) {
    this.slideModeFabViewControllerCallbacks = slideModeFabViewControllerCallbacks
  }

  fun toolbarContract(toolbarContract: ToolbarContract) {
    this.toolbarContract = toolbarContract
  }

  fun lockUnlockSlidingPaneLayout(lock: Boolean) {
    slidingPaneLayout.setSlidingEnabled(!lock)
  }

  override fun handleBack(): Boolean {
    if (toolbarContract.onBackPressed()) {
      return true
    }

    if (::slidingPaneLayout.isInitialized && !slidingPaneLayout.isOpen) {
      slidingPaneLayout.open()
      return true
    }

    return super.handleBack()
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

    val boardDescriptor = args.getBoardDescriptorOrNull()
    val threadDescriptor = args.getThreadDescriptorOrNull()

    catalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSlideCatalogController(boardDescriptor))
    )

    threadControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSlideThreadController(threadDescriptor))
    )

    slidingPaneLayout.setOverhangSize(ChanSettings.OVERHANG_SIZE)
    slidingPaneLayout.setSlidingPaneLayoutDefaultState(boardDescriptor, threadDescriptor)

    slideModeFabViewControllerCallbacks.onSlidingPaneInitialState(slidingPaneLayout.isOpen)
    slidingPaneLayoutSlideHandler = SlidingPaneLayoutSlideHandler(slidingPaneLayout.isOpen)
    slidingPaneLayoutSlideHandler.addListener(slidingPaneLayoutSlideListener)

    slidingPaneLayout.setPanelSlideListener(slidingPaneLayoutSlideHandler)
  }

  override fun onControllerDestroyed() {
    super.onControllerDestroyed()

    if (::slidingPaneLayoutSlideHandler.isInitialized) {
      slidingPaneLayoutSlideHandler.removeListener(slidingPaneLayoutSlideListener)
    }

    if (::slidingPaneLayout.isInitialized) {
      slidingPaneLayout.setPanelSlideListener(null)
    }
  }

  override fun provideRecyclerView(
    recyclerView: PaddingAwareRecyclerView,
    controllerType: ControllerType
  ) {
    recyclerViewProvider.provideRecyclerView(recyclerView, controllerType)
  }

  override fun withdrawRecyclerView(
    recyclerView: PaddingAwareRecyclerView,
    controllerType: ControllerType
  ) {
    recyclerViewProvider.withdrawRecyclerView(recyclerView, controllerType)
  }

  override fun openBoard(boardDescriptor: BoardDescriptor) {
    val catalogController = getCatalogControllerOrNull()
      ?: return

    if (slidingPaneLayout.slidingEnabled() && !slidingPaneLayout.isOpen) {
      slidingPaneLayout.open()
    }

    (catalogController as CatalogNavigationContract).openBoard(boardDescriptor)
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    val threadController = getThreadControllerOrNull()
      ?: return

    if (slidingPaneLayout.slidingEnabled() && slidingPaneLayout.isOpen) {
      slidingPaneLayout.close()
    }

    (threadController as ThreadNavigationContract).openThread(threadDescriptor)
  }

  private fun createSlideThreadController(threadDescriptor: ThreadDescriptor?): SlideThreadController {
    return SlideThreadController.create(threadDescriptor).apply {
      recyclerViewProvider(this@SlideNavController)
      uiElementsControllerCallbacks(uiElementsControllerCallbacks)
      provideToolbarContract(toolbarContract)
    }
  }

  private fun createSlideCatalogController(boardDescriptor: BoardDescriptor?): SlideCatalogController {
    return SlideCatalogController.create(boardDescriptor).apply {
      recyclerViewProvider(this@SlideNavController)
      uiElementsControllerCallbacks(uiElementsControllerCallbacks)
      threadNavigationContract(this@SlideNavController)
      toolbarContract(toolbarContract)
    }
  }

  private fun SlidingPaneLayoutEx.setSlidingPaneLayoutDefaultState(
    boardDescriptor: BoardDescriptor?,
    threadDescriptor: ThreadDescriptor?
  ) {
    val isNowOpen = openOrCloseSlidingPane(threadDescriptor)
    fireSlidingPaneListeners(isNowOpen)
  }

  private fun SlidingPaneLayoutEx.openOrCloseSlidingPane(
    threadDescriptor: ThreadDescriptor?
  ): Boolean {
    if (threadDescriptor == null) {
      // Open the sliding pane (show catalog)
      if (!isOpen) {
        check(slidingEnabled()) { "Sliding must not be disabled here" }
        check(openPane()) { "Failed to open pane" }
      }

      return true
    }

    // Close sliding pane (show thread)
    if (isOpen) {
      check(closePane())
    }

    return false
  }

  private fun fireSlidingPaneListeners(open: Boolean) {
    if (open) {
      // Catalog controller focued
      getThreadControllerOrNull()?.onLostFocus()
      getCatalogControllerOrNull()?.onGainedFocus()

      slideCatalogUiElementsControllerCallbacks.onControllerGainedFocus(isCatalogController = true)
    } else {
      // Thread controller focused
      getCatalogControllerOrNull()?.onLostFocus()
      getThreadControllerOrNull()?.onGainedFocus()

      slideCatalogUiElementsControllerCallbacks.onControllerGainedFocus(isCatalogController = false)
    }
  }

  private fun getThreadControllerOrNull(): SlideThreadController? {
    return threadControllerContainer.getControllerByTag(SlideThreadController.CONTROLLER_TAG)
  }

  private fun getCatalogControllerOrNull(): SlideCatalogController? {
    return catalogControllerContainer.getControllerByTag(SlideCatalogController.CONTROLLER_TAG)
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  interface SlideCatalogUiElementsControllerCallbacks {
    fun onBeforeSliding(transitioningIntoCatalogToolbar: Boolean)
    fun onSliding(offset: Float)
    fun onAfterSliding(becameCatalogToolbar: Boolean)
    fun onControllerGainedFocus(isCatalogController: Boolean)
  }

  companion object {
    private const val TAG = "SlideNavController"

    val CONTROLLER_TAG = ControllerTag("SlideNavControllerTag")

    fun create(boardDescriptor: BoardDescriptor?, threadDescriptor: ThreadDescriptor?): SlideNavController {
      val args = Bundle()
      args.putBoardDescriptor(boardDescriptor)
      args.putThreadDescriptor(threadDescriptor)

      return SlideNavController(args)
    }
  }
}