package com.github.k1rakishou.kurobanewnavstacktest.core

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.CollapsingViewController
import com.github.k1rakishou.kurobanewnavstacktest.viewcontroller.ViewScreenAttachSide

class CollapsingViewsHolder {
  private val collapsingViewControllerMap =
    mutableMapOf<View, MutableMap<RecyclerView, CollapsingViewController>>()

  fun detach(recyclerView: RecyclerView, collapsableView: View) {
    collapsingViewControllerMap[collapsableView]?.remove(recyclerView)?.detach(recyclerView)

    if (collapsingViewControllerMap[collapsableView].isNullOrEmpty()) {
      collapsingViewControllerMap.remove(collapsableView)
    }
  }

  fun attach(
    recyclerView: RecyclerView,
    collapsableView: View,
    viewAttachSide: ViewScreenAttachSide
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
  }

  fun lockUnlockCollapsableViews(recyclerView: RecyclerView?, lock: Boolean, animate: Boolean) {
    for ((_, innerMap) in collapsingViewControllerMap.entries) {
      for ((rv, collapsingViewController) in innerMap) {
        if (recyclerView != null && recyclerView !== rv) {
          continue
        }

        collapsingViewController.lockUnlock(lock = lock, animate = animate)
      }
    }
  }

}