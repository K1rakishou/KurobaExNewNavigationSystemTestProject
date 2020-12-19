package com.github.k1rakishou.kurobanewnavstacktest.core

import androidx.recyclerview.widget.RecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.CollapsingViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide
import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

class CollapsingViewsHolder {
  private val collapsingViewControllerMap =
    mutableMapOf<CollapsableView, MutableMap<RecyclerView, CollapsingViewController>>()
  private val recyclers = mutableMapOf<ControllerType, PaddingAwareRecyclerView>()

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

  fun lockUnlockCollapsableViews(recyclerView: PaddingAwareRecyclerView?, lock: Boolean, animate: Boolean) {
    for ((_, innerMap) in collapsingViewControllerMap.entries) {
      for ((rv, collapsingViewController) in innerMap) {
        if (recyclerView != null && recyclerView !== rv) {
          continue
        }

        collapsingViewController.lockUnlock(lock = lock, animate = animate)
      }
    }
  }

  fun getRecyclerForController(controllerType: ControllerType): PaddingAwareRecyclerView? {
    return recyclers[controllerType]
  }

}