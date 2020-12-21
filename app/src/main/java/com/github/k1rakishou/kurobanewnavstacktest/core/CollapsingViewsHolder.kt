package com.github.k1rakishou.kurobanewnavstacktest.core

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.CollapsingViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanelStateKind
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

class CollapsingViewsHolder(
  private val context: Context
) {
  private val collapsingViewControllerMap =
    mutableMapOf<CollapsableView, MutableMap<RecyclerView, CollapsingViewController>>()
  private val recyclers = mutableMapOf<ControllerType, PaddingAwareRecyclerView>()
  private val state = mutableMapOf<ControllerType, CollapsingViewState>()

  init {
    state[ControllerType.Catalog] = CollapsingViewState(context)
    state[ControllerType.Thread] = CollapsingViewState(context)
  }

  fun detach(
    recyclerView: RecyclerView,
    collapsableView: CollapsableView,
    controllerType: ControllerType
  ) {
    collapsingViewControllerMap[collapsableView]?.remove(recyclerView)?.detach(recyclerView)

    if (collapsingViewControllerMap[collapsableView].isNullOrEmpty()) {
      collapsingViewControllerMap.remove(collapsableView)
    }

    recyclers.remove(controllerType)
  }

  fun attach(
    recyclerView: PaddingAwareRecyclerView,
    collapsableView: CollapsableView,
    viewAttachSide: ViewScreenAttachSide,
    controllerType: ControllerType
  ) {
    if (!collapsingViewControllerMap.containsKey(collapsableView)) {
      collapsingViewControllerMap[collapsableView] = mutableMapOf()
    }

    if (!collapsingViewControllerMap[collapsableView]!!.containsKey(recyclerView)) {
      collapsingViewControllerMap[collapsableView]!![recyclerView] = CollapsingViewController(
        recyclerView.context,
        viewAttachSide
      )
    }

    collapsingViewControllerMap[collapsableView]!![recyclerView]!!.attach(collapsableView, recyclerView)
    recyclers.put(controllerType, recyclerView)
  }

  fun getRecyclerForController(controllerType: ControllerType): PaddingAwareRecyclerView? {
    return recyclers[controllerType]
  }

  fun toolbarSearchVisibilityChanged(controllerType: ControllerType, toolbarSearchVisible: Boolean) {
    val controllerCollapsingViewState = state[controllerType]
      ?: return

    if (controllerCollapsingViewState.searchToolbarShown == toolbarSearchVisible) {
      return
    }

    controllerCollapsingViewState.searchToolbarShown = toolbarSearchVisible
    onCollapsingViewStateChanged(controllerType)
  }

  fun onBottomPanelStateChanged(controllerType: ControllerType, stateKind: KurobaBottomPanelStateKind) {
    val controllerCollapsingViewState = state[controllerType]
      ?: return

    if (controllerCollapsingViewState.bottomPanelStateKind == stateKind) {
      return
    }

    controllerCollapsingViewState.bottomPanelStateKind = stateKind
    onCollapsingViewStateChanged(controllerType)
  }

  private fun onCollapsingViewStateChanged(controllerType: ControllerType) {
    val controllerCollapsingViewState = state[controllerType]
      ?: return

    val attachedRecyclerView = recyclers[controllerType]
      ?: return

    val shouldCollapsingViewsBeLocked = controllerCollapsingViewState.overrideCollapsingViewsLockState
      || controllerCollapsingViewState.searchToolbarShown
      || controllerCollapsingViewState.bottomPanelStateKind in COLLAPSING_VIEWS_LOCKED_BOTTOM_PANEL_STATE

    lockUnlockCollapsableViews(
      recyclerView = attachedRecyclerView,
      lock = shouldCollapsingViewsBeLocked,
      animate = true
    )
  }

  private fun lockUnlockCollapsableViews(
    recyclerView: PaddingAwareRecyclerView?,
    lock: Boolean,
    animate: Boolean
  ) {
    for ((_, innerMap) in collapsingViewControllerMap.entries) {
      for ((rv, collapsingViewController) in innerMap) {
        if (recyclerView != null && recyclerView !== rv) {
          continue
        }

        collapsingViewController.lockUnlock(lock = lock, animate = animate)
      }
    }
  }

  class CollapsingViewState(
    context: Context,
    val overrideCollapsingViewsLockState: Boolean = ChanSettings.collapsibleViewsAlwaysLocked(context),
    var searchToolbarShown: Boolean = false,
    var bottomPanelStateKind: KurobaBottomPanelStateKind = KurobaBottomPanelStateKind.Uninitialized
  )

  companion object {
    private val COLLAPSING_VIEWS_LOCKED_BOTTOM_PANEL_STATE = arrayOf(
      KurobaBottomPanelStateKind.Uninitialized,
      KurobaBottomPanelStateKind.ReplyLayoutPanel,
      KurobaBottomPanelStateKind.SelectionPanel
    )
  }

}