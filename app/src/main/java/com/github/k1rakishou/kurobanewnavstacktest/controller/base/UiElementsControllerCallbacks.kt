package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

interface UiElementsControllerCallbacks {
  fun lockUnlockCollapsableViews(recyclerView: PaddingAwareRecyclerView?, lock: Boolean, animate: Boolean)
  fun showFab(lock: Boolean? = null)
  fun hideFab(lock: Boolean? = null)
}