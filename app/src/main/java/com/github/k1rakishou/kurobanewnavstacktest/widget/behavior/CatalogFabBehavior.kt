package com.github.k1rakishou.kurobanewnavstacktest.widget.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class CatalogFabBehavior(
  context: Context,
  attributeSet: AttributeSet?
) :
  CoordinatorLayout.Behavior<KurobaFloatingActionButton>(context, attributeSet) {
  private var snackBarVisible = false

  fun reset() {
    snackBarVisible = false
  }

  override fun layoutDependsOn(
    parent: CoordinatorLayout,
    child: KurobaFloatingActionButton,
    dependency: View
  ): Boolean {
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
    }
  }
}
