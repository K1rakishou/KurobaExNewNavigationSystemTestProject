package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndRequest
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract

abstract class ThreadController(args: Bundle? = null) : BaseController(args) {
  protected lateinit var recyclerView: EpoxyRecyclerView
  protected lateinit var toolbarContract: ToolbarContract

  final override fun instantiateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_thread, container) {
      recyclerView = findViewById(R.id.controller_thread_epoxy_recycler_view)
      toolbarContract = findViewById(R.id.thread_controller_toolbar)
    }
  }

  override fun onControllerCreated(savedViewState: Bundle?) {
    super.onControllerCreated(savedViewState)

    applyInsetsForRecyclerView()
  }

  private fun applyInsetsForRecyclerView() {
    val toolbarHeight = currentContext().resources.getDimension(R.dimen.toolbar_height).toInt()
    val bottomNavViewHeight =
      currentContext().resources.getDimension(R.dimen.bottom_nav_view_height).toInt()

    recyclerView.setOnApplyWindowInsetsListenerAndRequest { v, insets ->
      v.updatePadding(
        top = toolbarHeight + insets.systemWindowInsetTop,
        bottom = bottomNavViewHeight + insets.systemWindowInsetBottom
      )

      return@setOnApplyWindowInsetsListenerAndRequest insets
    }
  }
}