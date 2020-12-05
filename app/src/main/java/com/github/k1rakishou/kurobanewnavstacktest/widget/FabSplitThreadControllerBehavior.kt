package com.github.k1rakishou.kurobanewnavstacktest.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.k1rakishou.kurobanewnavstacktest.utils.findChildView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class FabSplitThreadControllerBehavior(
    context: Context,
    attributeSet: AttributeSet?
) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attributeSet) {
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
      child: FloatingActionButton,
      dependency: View
  ): Boolean {
    if (dependency is MaterialToolbar) {
      return true
    }

    if (dependency is Snackbar.SnackbarLayout) {
      return true
    }

    return super.layoutDependsOn(parent, child, dependency)
  }

  override fun onDependentViewChanged(
      parent: CoordinatorLayout,
      child: FloatingActionButton,
      dependency: View
  ): Boolean {
    if (dependency is MaterialToolbar) {
      if (initialPositionY == null) {
        return false
      }

      resolveMaterialToolbarView(parent, child, dependency)
      return true
    }

    if (dependency is Snackbar.SnackbarLayout) {
      snackBarVisible = true
      child.hide()
      return false
    }

    return super.onDependentViewChanged(parent, child, dependency)
  }

  override fun onDependentViewRemoved(
      parent: CoordinatorLayout,
      child: FloatingActionButton,
      dependency: View
  ) {
    super.onDependentViewRemoved(parent, child, dependency)

    if (dependency is Snackbar.SnackbarLayout) {
      snackBarVisible = false

      if (initialPositionY == null) {
        return
      }

      val materialToolbarView = findChildView(parent) { view ->
        view is MaterialToolbar
      } as? MaterialToolbar ?: return

      if (materialToolbarView.translationY.toInt() == initialPositionY) {
        child.show()
      }
    }
  }

  private fun resolveMaterialToolbarView(
      parent: CoordinatorLayout,
      child: FloatingActionButton,
      dependency: MaterialToolbar
  ) {
    if (snackBarVisible) {
      return
    }

    val initialPosY = initialPositionY
      ?: return

    if (dependency.translationY.toInt() == initialPositionY && child.isOrWillBeHidden) {
      child.show()
    }

    val scale = 1f - (Math.abs(dependency.translationY - initialPosY) / dependency.height)
    child.scaleX = scale
    child.scaleY = scale
  }

}