package com.github.k1rakishou.kurobanewnavstacktest.controller

import com.github.k1rakishou.kurobanewnavstacktest.widget.recycler.PaddingAwareRecyclerView

interface RecyclerViewProvider {
  fun provideRecyclerView(recyclerView: PaddingAwareRecyclerView, controllerType: ControllerType)
  fun withdrawRecyclerView(recyclerView: PaddingAwareRecyclerView, controllerType: ControllerType)
}