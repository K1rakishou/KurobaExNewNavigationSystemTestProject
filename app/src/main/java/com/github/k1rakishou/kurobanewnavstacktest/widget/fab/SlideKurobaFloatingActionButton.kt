package com.github.k1rakishou.kurobanewnavstacktest.widget.fab

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings

class SlideKurobaFloatingActionButton @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null
) : KurobaFloatingActionButton(context, attributeSet) {
  private var focusedControllerType: ControllerType? = null
  private var stateArray: Array<FabState>

  init {
    stateArray = createDefaultState()

    stateArray[0].locked = ChanSettings.collapsibleViewsAlwaysLocked(context)
    stateArray[1].locked = ChanSettings.collapsibleViewsAlwaysLocked(context)
  }

  private fun createDefaultState() = arrayOf(
    FabState(), // Catalog controller
    FabState(), // Thread controller
  )

  fun onControllerFocused(focusedControllerType: ControllerType?) {
    this.focusedControllerType = focusedControllerType

    if (focusedControllerType == null) {
      return
    }

    val state = getState(focusedControllerType)
    if (state.shown) {
      showFab()
    } else {
      hideFab()
    }
  }

  override fun setScale(sx: Float, sy: Float) {
    val controllerType = focusedControllerType
      ?: return

    val state = getState(controllerType)
    if (state.locked) {
      return
    }

    if (!state.shown) {
      return
    }

    scaleX = sx
    scaleY = sy
  }

  override fun hideFab(lock: Boolean?) {
    val controllerType = focusedControllerType
      ?: return

    val state = getState(controllerType)
    if (state.locked && lock == null) {
      return
    }

    if (lock != null && canUnlock()) {
      state.locked = lock
    }

    if (isOrWillBeHidden) {
      return
    }

    state.shown = false
    hide()
  }

  override fun showFab(lock: Boolean?) {
    val controllerType = focusedControllerType
      ?: return

    val state = getState(controllerType)
    if (state.locked && lock == null) {
      return
    }

    if (lock != null && canUnlock()) {
      state.locked = lock
    }

    if (isOrWillBeShown) {
      return
    }

    state.shown = true
    show()
  }

  override fun onSaveInstanceState(): Parcelable {
    val bundle = Bundle()
    bundle.putParcelable(SUPER_VIEW_STATE_KEY, super.onSaveInstanceState())
    bundle.putParcelableArray(SLIDE_FAB_STATE, stateArray)

    return bundle
  }

  @Suppress("UNCHECKED_CAST")
  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state !is Bundle) {
      super.onRestoreInstanceState(state)
      return
    }

    val superState = state.getParcelable<Parcelable>(SUPER_VIEW_STATE_KEY)

    stateArray = state.getParcelableArray(SLIDE_FAB_STATE)
      as? Array<FabState>
      ?: createDefaultState()

    super.onRestoreInstanceState(superState)
  }

  private fun getState(controllerType: ControllerType): FabState {
    return when (controllerType) {
      ControllerType.Catalog -> stateArray[0]
      ControllerType.Thread -> stateArray[1]
    }
  }

}