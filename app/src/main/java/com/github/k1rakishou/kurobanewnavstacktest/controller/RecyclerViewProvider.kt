package com.github.k1rakishou.kurobanewnavstacktest.controller

import androidx.recyclerview.widget.RecyclerView

interface RecyclerViewProvider {
  fun provideRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType)
  fun withdrawRecyclerView(recyclerView: RecyclerView, controllerType: ControllerType)
}