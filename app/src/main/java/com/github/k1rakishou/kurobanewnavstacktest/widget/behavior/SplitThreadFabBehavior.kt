package com.github.k1rakishou.kurobanewnavstacktest.widget.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.k1rakishou.kurobanewnavstacktest.utils.findChildView
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarContract
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class SplitThreadFabBehavior(
    context: Context,
    attributeSet: AttributeSet?
) : CoordinatorLayout.Behavior<KurobaFloatingActionButton>(context, attributeSet) {
  private var initialPositionY: Int? = null
  private var snackBarVisible = false

  fun init(laidOutFab: FloatingActionButton) {
    require(laidOutFab.isLaidOut) { "FloatingActionButton is not laid out!" }

    this.initialPositionY = laidOutFab.translationY.toInt()
  }

  fun reset() {
    initialPositionY = null
    snackBarVisible = false
  }

  override fun layoutDependsOn(
      parent: CoordinatorLayout,
      child: KurobaFloatingActionButton,
      dependency: View
  ): Boolean {
    if (dependency is ToolbarContract) {
      return true
    }

    if (dependency is Snackbar.SnackbarLayout) {
      return true
    }

    return super.layoutDependsOn(parent, child, dependency)
  }

  override fun onDependentViewChanged(
      parent: CoordinatorLayout,
      child: KurobaFloatingActionButton,
      dependency: View
  ): Boolean {
    if (dependency is ToolbarContract) {
      if (initialPositionY == null) {
        return false
      }

      resolveMaterialToolbarView(parent, child, dependency)
      return true
    }

    if (dependency is Snackbar.SnackbarLayout) {
      snackBarVisible = true
      child.hideFab()

      return false
    }

    return super.onDependentViewChanged(parent, child, dependency)
  }

  override fun onDependentViewRemoved(
      parent: CoordinatorLayout,
      child: KurobaFloatingActionButton,
      dependency: View
  ) {
    super.onDependentViewRemoved(parent, child, dependency)

    if (dependency is Snackbar.SnackbarLayout) {
      snackBarVisible = false

      if (initialPositionY == null) {
        return
      }

      val toolbarContract = findChildView(parent) { view -> view is ToolbarContract }
        as? ToolbarContract
        ?: return

      val toolbarView = toolbarContract.collapsableView()
      if (toolbarView.translationY().toInt() == initialPositionY) {
        child.showFab()
      }
    }
  }

  private fun resolveMaterialToolbarView(
      parent: CoordinatorLayout,
      child: KurobaFloatingActionButton,
      toolbarContract: ToolbarContract
  ) {
    if (snackBarVisible) {
      return
    }

    val initialPosY = initialPositionY
      ?: return

    val toolbarView = toolbarContract.collapsableView()
    if (toolbarView.translationY().toInt() == initialPositionY && child.isOrWillBeHidden) {
      child.showFab()
    }

    val scale = 1f - (Math.abs(toolbarView.translationY() - initialPosY) / toolbarView.height())
    child.setScale(scale, scale)
  }

}