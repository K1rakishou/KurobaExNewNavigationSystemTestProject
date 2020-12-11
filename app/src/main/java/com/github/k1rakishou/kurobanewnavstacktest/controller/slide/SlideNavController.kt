package com.github.k1rakishou.kurobanewnavstacktest.controller.slide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.RecyclerViewProvider
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.*
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import com.github.k1rakishou.kurobanewnavstacktest.utils.ScreenOrientationUtils
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.SlideModeFabViewControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.widget.SlidingPaneLayoutEx
import com.github.k1rakishou.kurobanewnavstacktest.widget.SlidingPaneLayoutSlideHandler
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

class SlideNavController(
  args: Bundle? = null
) : BaseController(args),
  RecyclerViewProvider,
  UiElementsControllerCallbacks,
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

  override fun handleBack(): Boolean {
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

    catalogControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSlideCatalogController())
    )

    threadControllerContainer.setupChildRouterIfNotSet(
      RouterTransaction.with(createSlideThreadController())
    )

    slidingPaneLayout.setOverhangSize(ChanSettings.OVERHANG_SIZE)
    slidingPaneLayout.setSlidingPaneLayoutDefaultState()

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

  override fun showFab() {
    uiElementsControllerCallbacks.showFab()
  }

  override fun hideFab() {
    uiElementsControllerCallbacks.hideFab()
  }

  override fun provideRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
    recyclerViewProvider.provideRecyclerView(recyclerView, controllerType)
  }

  override fun withdrawRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType) {
    recyclerViewProvider.withdrawRecyclerView(recyclerView, controllerType)
  }

  override fun openBoard(boardDescriptor: BoardDescriptor) {
    val catalogController = getCatalogControllerOrNull()
      ?: return

    if (!slidingPaneLayout.isOpen) {
      slidingPaneLayout.open()
    }

    (catalogController as CatalogNavigationContract).openBoard(boardDescriptor)
  }

  override fun openThread(threadDescriptor: ThreadDescriptor) {
    val threadController = getThreadControllerOrNull()
      ?: return

    if (slidingPaneLayout.isOpen) {
      slidingPaneLayout.close()
    }

    (threadController as ThreadNavigationContract).openThread(threadDescriptor)
  }

  private fun createSlideThreadController(): SlideThreadController {
    return SlideThreadController().apply {
      recyclerViewProvider(this@SlideNavController)
      toolbarContract(toolbarContract)
    }
  }

  private fun createSlideCatalogController(): SlideCatalogController {
    return SlideCatalogController().apply {
      recyclerViewProvider(this@SlideNavController)
      uiElementsControllerCallbacks(this@SlideNavController)
      threadNavigationContract(this@SlideNavController)
      toolbarContract(toolbarContract)
    }
  }

  private fun SlidingPaneLayoutEx.setSlidingPaneLayoutDefaultState() {
    open()
    fireSlidingPaneListeners(isOpen)
  }

  private fun fireSlidingPaneListeners(open: Boolean) {
    if (open) {
      getThreadControllerOrNull()?.onLostFocus()
      getCatalogControllerOrNull()?.onGainedFocus()

      slideCatalogUiElementsControllerCallbacks.onControllerGainedFocus(isCatalogController = true)
    } else {
      getCatalogControllerOrNull()?.onLostFocus()
      getThreadControllerOrNull()?.onGainedFocus()

      slideCatalogUiElementsControllerCallbacks.onControllerGainedFocus(isCatalogController = false)
    }
  }

  private fun getThreadControllerOrNull(): SlideThreadController? {
    return threadControllerContainer.getControllerByTag(SlideThreadController.CONTROLLER_TAG)
  }

  private fun getCatalogControllerOrNull(): SlideCatalogController? {
    return threadControllerContainer.getControllerByTag(SlideCatalogController.CONTROLLER_TAG)
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
  }
}