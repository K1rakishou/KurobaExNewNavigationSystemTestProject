package com.github.k1rakishou.kurobanewnavstacktest.widget.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp
import com.github.k1rakishou.kurobanewnavstacktest.utils.findChildView
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class CatalogFabBehavior(context: Context, attributeSet: AttributeSet?) :
  CoordinatorLayout.Behavior<KurobaFloatingActionButton>(context, attributeSet) {
  private var initialPositionY: Int? = null
  private var snackBarVisible = false

  fun init(laidOutBottomNavigationView: BottomNavigationView) {
    require(laidOutBottomNavigationView.isLaidOut) { "BottomNavigationView is not laid out!" }

    this.initialPositionY = laidOutBottomNavigationView.y.toInt()
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
    if (dependency is BottomNavigationView) {
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
    if (dependency is BottomNavigationView) {
      if (initialPositionY == null) {
        return false
      }

      resolveBottomNavigationView(parent, child, dependency)
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

      val bottomNavView = findChildView(parent) { view ->
        view is BottomNavigationView
      } as? BottomNavigationView ?: return

      if (bottomNavView.y.toInt() == initialPositionY) {
        child.showFab()
      }
    }
  }

  private fun resolveBottomNavigationView(
    parent: CoordinatorLayout,
    child: KurobaFloatingActionButton,
    dependency: BottomNavigationView
  ) {
    if (snackBarVisible) {
      return
    }

    val initialPosY = initialPositionY
      ?: return

    if (dependency.y.toInt() == initialPosY && child.isOrWillBeHidden) {
      child.showFab()
    }

    child.y = (initialPosY - child.height - FAB_TO_VIEW_BOTTOM_MARGIN).toFloat()

    val scale = 1f - (Math.abs(dependency.y - initialPosY) / dependency.height)
    if (scale < 0f || scale > 1f) {
      return
    }

    child.setScale(scale, scale)
    return
  }

  companion object {
    private val FAB_TO_VIEW_BOTTOM_MARGIN = 16.dp
  }
}
