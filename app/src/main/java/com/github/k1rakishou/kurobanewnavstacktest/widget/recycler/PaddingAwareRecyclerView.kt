package com.github.k1rakishou.kurobanewnavstacktest.widget.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnApplyWindowInsetsListenerAndDoRequest

class PaddingAwareRecyclerView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defAttrStyle: Int = 0
) : EpoxyRecyclerView(context, attributeSet, defAttrStyle) {
  private val defaultPanelHeight = context.resources.getDimension(R.dimen.bottom_nav_panel_height).toInt()
  private val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()

  private var prevBottomPanelHeight: Int = -1
  private var prevTopInset: Int = 0
  private var prevBottomInset: Int = 0

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val globalWindowInsetsManager = (context as MainActivity).globalWindowInsetsManager
    prevTopInset = globalWindowInsetsManager.topInset
    prevBottomInset = globalWindowInsetsManager.bottomInset

    setOnApplyWindowInsetsListenerAndDoRequest { view, insets ->
      val bottomNavViewHeight = if (prevBottomPanelHeight < 0) {
        defaultPanelHeight
      } else {
        prevBottomPanelHeight
      }

      prevTopInset = insets.systemWindowInsetTop
      prevBottomInset = insets.systemWindowInsetBottom
      prevBottomPanelHeight = bottomNavViewHeight

      (view as RecyclerView).updateRecyclerPaddings()

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }
  }

  fun updatePanelHeight(newHeight: Int) {
    if (newHeight == prevBottomPanelHeight) {
      return
    }

    prevBottomPanelHeight = newHeight
    updateRecyclerPaddings()
  }

  private fun RecyclerView.updateRecyclerPaddings() {
    updatePadding(
      top = toolbarHeight + prevTopInset,
      bottom = prevBottomPanelHeight + prevBottomInset
    )
  }

}