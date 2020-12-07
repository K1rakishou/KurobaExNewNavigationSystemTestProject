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
    controllerType: ControllerType,
    viewAttachSide: ViewScreenAttachSide
  ) {
    if (!collapsingViewControllerMap.containsKey(collapsableView)) {
      collapsingViewControllerMap[collapsableView] = mutableMapOf()
    }

    if (!collapsingViewControllerMap[collapsableView]!!.containsKey(recyclerView)) {
      collapsingViewControllerMap[collapsableView]!![recyclerView] = CollapsingViewController(
        controllerType,
        viewAttachSide
      )
    }

    collapsingViewControllerMap[collapsableView]!![recyclerView]!!.attach(collapsableView, recyclerView)
  }

  fun lockUnlockCollapsableViews(lock: Boolean, animate: Boolean) {
    collapsingViewControllerMap.values
      .flatMap { innerMap -> innerMap.values }
      .forEach { collapsingViewDelegate ->
        collapsingViewDelegate.lockUnlock(
          lock = lock,
          animate = animate
        )
      }
  }

}