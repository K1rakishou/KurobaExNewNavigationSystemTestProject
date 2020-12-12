package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import androidx.recyclerview.widget.RecyclerView

interface UiElementsControllerCallbacks {
  fun lockUnlockCollapsableViews(recyclerView: RecyclerView?, lock: Boolean, animate: Boolean)
  fun showFab(lock: Boolean? = null)
  fun hideFab(lock: Boolean? = null)
}