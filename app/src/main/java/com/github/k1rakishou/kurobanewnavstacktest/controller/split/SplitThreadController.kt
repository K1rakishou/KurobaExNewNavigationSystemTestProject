package com.github.k1rakishou.kurobanewnavstacktest.controller.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.feature.thread.ThreadController
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.UiElementsControllerCallbacks
import com.github.k1rakishou.kurobanewnavstacktest.core.CollapsingViewsHolder
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadData
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.getThreadDescriptorOrNull
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.putThreadDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanelStateKind
import com.github.k1rakishou.kurobanewnavstacktest.widget.behavior.SplitThreadFabBehavior
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.NormalToolbar

class SplitThreadController(
  args: Bundle? = null
) : ThreadController(args), UiElementsControllerCallbacks {
  private lateinit var threadFab: KurobaFloatingActionButton
  private lateinit var bottomPanel: KurobaBottomPanel

  private val collapsingViewsHolder by lazy { CollapsingViewsHolder(currentContext()) }
  private val splitFabViewController by lazy { activityContract().mainActivityOrError().splitFabViewController }

  override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return super.instantiateView(inflater, container, savedViewState).apply {
      threadFab = findViewById(R.id.split_controller_thread_fab)
      bottomPanel = findViewById(R.id.split_thread_controller_bottom_panel)

      threadFab.setBehaviorExt(SplitThreadFabBehavior(currentContext(), null))
      threadFab.setOnClickListener { bottomPanel.switchInto(KurobaBottomPanelStateKind.ReplyLayoutPanel) }
      threadFab.hide()

      val normalToolbar = findViewById<NormalToolbar>(R.id.thread_controller_toolbar)
      normalToolbar.init(KurobaToolbarType.Thread)
      normalToolbar.visibility = View.VISIBLE

      super.provideToolbarContract(normalToolbar)
      super.uiElementsControllerCallbacks(this@SplitThreadController)

      splitFabViewController.setThreadFab(threadFab)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    threadFab.setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      threadFab.updateMargins(
        end = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT + insets.systemWindowInsetRight,
        bottom = KurobaFloatingActionButton.DEFAULT_MARGIN_RIGHT
      )

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }

    initBottomPanel()

    args.getThreadDescriptorOrNull()?.let { threadDescriptor -> openThread(threadDescriptor) }
  }

  private fun initBottomPanel() {
    bottomPanel.attachFab(threadFab)
    bottomPanel.addOnBottomPanelInitialized {
      // Must be called before onBottomPanelInitialized
      threadFab.initialized()
      splitFabViewController.onBottomPanelInitialized(controllerType)
    }
    bottomPanel.addOnBottomPanelStateChanged { panelControllerType, newState ->
      check(panelControllerType == controllerType) {
        "Unexpected panelControllerType: $panelControllerType"
      }

      splitFabViewController.onBottomPanelStateChanged(panelControllerType, newState)
      onBottomPanelStateChanged(panelControllerType, newState)
    }
    bottomPanel.addOnBottomPanelHeightChangeListener { controllerType, panelHeight ->
      collapsingViewsHolder.getRecyclerForController(controllerType)?.updatePanelHeight(panelHeight)
    }

    bottomPanel.bottomPanelPreparationsCompleted(
      controllerType,
      KurobaBottomPanelStateKind.Hidden,
      onBottomPanelInitializedCallback = {
        bottomPanel.onControllerFocused(controllerType)
      }
    )
  }

  override fun myHandleBack(): Boolean {
    if (isToolbarContractInitialized && toolbarContract.onBackPressed()) {
      return true
    }

    if (::bottomPanel.isInitialized && bottomPanel.onBackPressed()) {
      return true
    }

    return super.myHandleBack()
  }

  override fun onControllerShown() {
    super.onControllerShown()

    threadFab.doOnPreDraw {
      threadFab.getBehaviorExt<SplitThreadFabBehavior>()?.apply {
        reset()
      }
    }
  }

  override fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    recyclerView.doOnPreDraw {
      bottomPanel.onPanelAvailableVerticalSpaceKnown(controllerType, recyclerView.height)

      collapsingViewsHolder.attach(
        recyclerView = recyclerView,
        collapsableView = toolbarContract.collapsableView(),
        viewAttachSide = ViewScreenAttachSide.Top,
        controllerType = ControllerType.Thread
      )
    }
  }

  override fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView) {
    collapsingViewsHolder.detach(
      recyclerView = recyclerView,
      collapsableView = toolbarContract.collapsableView(),
      controllerType = ControllerType.Thread
    )
  }

  override fun toolbarSearchVisibilityChanged(
    controllerType: ControllerType,
    toolbarSearchVisible: Boolean
  ) {
    collapsingViewsHolder.toolbarSearchVisibilityChanged(controllerType, toolbarSearchVisible)
  }

  override fun onBottomPanelStateChanged(
    controllerType: ControllerType,
    stateKind: KurobaBottomPanelStateKind
  ) {
    collapsingViewsHolder.onBottomPanelStateChanged(controllerType, stateKind)
  }

  override fun showFab(lock: Boolean?) {
    threadFab.showFab(lock)
  }

  override fun hideFab(lock: Boolean?) {
    threadFab.hideFab(lock)
  }

  override fun onSearchToolbarShown() {
    splitFabViewController.onSearchToolbarShownOrHidden(controllerType, true)
  }

  override fun onSearchToolbarHidden() {
    splitFabViewController.onSearchToolbarShownOrHidden(controllerType, false)
  }

  override fun onThreadStateChanged(threadData: ThreadData) {
    splitFabViewController.onControllerStateChanged(
      controllerType,
      threadData is ThreadData.Data
    )
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG

  companion object {
    val CONTROLLER_TAG = ControllerTag("SplitThreadControllerTag")

    fun create(threadDescriptor: ThreadDescriptor?): SplitThreadController {
      val args = Bundle()
      args.putThreadDescriptor(threadDescriptor)

      return SplitThreadController(args)
    }
  }

}