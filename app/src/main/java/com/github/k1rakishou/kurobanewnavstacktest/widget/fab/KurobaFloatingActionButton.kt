package com.github.k1rakishou.kurobanewnavstacktest.widget.fab

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp
import com.google.android.material.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

open class KurobaFloatingActionButton @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defAttrStyle: Int = R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attributeSet, defAttrStyle) {
  private val fabState = FabState()
  protected var initialized = false

  init {
    fabState.locked = ChanSettings.collapsibleViewsAlwaysLocked(context)
  }

  fun initialized() {
    this.initialized = true
  }

  open fun setScale(sx: Float, sy: Float) {
    if (fabState.locked) {
      return
    }

    if (!initialized) {
      return
    }

    scaleX = sx
    scaleY = sy
  }

  open fun hideFab(lock: Boolean? = null) {
    if (fabState.locked && lock == null) {
      return
    }

    if (lock != null && canUnlock()) {
      fabState.locked = lock
    }

    if (!initialized) {
      return
    }

    fabState.shown = false
    hide()
  }

  open fun showFab(lock: Boolean? = null) {
    if (fabState.locked && lock == null) {
      return
    }

    if (lock != null && canUnlock()) {
      fabState.locked = lock
    }

    if (!initialized) {
      return
    }

    fabState.shown = true
    show()
  }

  override fun onSaveInstanceState(): Parcelable {
    val bundle = Bundle()
    bundle.putParcelable(SPLIT_SUPER_VIEW_STATE_KEY, super.onSaveInstanceState())
    bundle.putParcelable(SPLIT_FAB_STATE, fabState)

    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state !is Bundle) {
      super.onRestoreInstanceState(state)
      return
    }

    val prevFabState = state.getParcelable<FabState>(SPLIT_FAB_STATE)
    if (prevFabState != null) {
      if (fabState.shown) {
        showFab(fabState.locked)
      } else {
        hideFab(fabState.locked)
      }
    }

    val superState = state.getParcelable<Parcelable>(SPLIT_SUPER_VIEW_STATE_KEY)
    super.onRestoreInstanceState(superState)
  }

  protected fun canUnlock(): Boolean {
    return !ChanSettings.collapsibleViewsAlwaysLocked(context)
  }

  companion object {
    val DEFAULT_MARGIN_RIGHT = 16.dp

    private val SPLIT_SUPER_VIEW_STATE_KEY = "split_super_state"
    private val SPLIT_FAB_STATE = "split_fab_state"
  }

}