package com.github.k1rakishou.kurobanewnavstacktest.widget.fab

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp
import com.google.android.material.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

open class KurobaFloatingActionButton @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defAttrStyle: Int = R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attributeSet, defAttrStyle) {
  private val fabState = FabState()

  protected val SUPER_VIEW_STATE_KEY = "super_state"
  protected val SLIDE_FAB_STATE = "slide_fab_state"

  open fun setScale(sx: Float, sy: Float) {
    if (fabState.locked) {
      return
    }

    scaleX = sx
    scaleY = sy
  }

  open fun hideFab(lock: Boolean? = null) {
    if (fabState.locked && lock == null) {
      return
    }

    if (lock != null) {
      fabState.locked = lock
    }

    if (isOrWillBeHidden) {
      return
    }

    fabState.shown = false
    hide()
  }

  open fun showFab(lock: Boolean? = null) {
    if (fabState.locked && lock == null) {
      return
    }

    if (lock != null) {
      fabState.locked = lock
    }

    if (isOrWillBeShown) {
      return
    }

    fabState.shown = true
    show()
  }

  override fun onSaveInstanceState(): Parcelable {
    val bundle = Bundle()
    bundle.putParcelable(SUPER_VIEW_STATE_KEY, super.onSaveInstanceState())
    bundle.putParcelable(SLIDE_FAB_STATE, fabState)

    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state !is Bundle) {
      super.onRestoreInstanceState(state)
      return
    }

    val prevFabState = state.getParcelable<FabState>(SLIDE_FAB_STATE)
    if (prevFabState != null) {
      fabState.locked = prevFabState.locked
    }

    val superState = state.getParcelable<Parcelable>(SUPER_VIEW_STATE_KEY)
    super.onRestoreInstanceState(superState)
  }

  companion object {
    val DEFAULT_MARGIN_RIGHT = 16.dp
  }

}