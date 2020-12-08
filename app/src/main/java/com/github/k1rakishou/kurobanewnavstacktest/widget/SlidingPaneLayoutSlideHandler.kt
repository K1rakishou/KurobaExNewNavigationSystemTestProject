package com.github.k1rakishou.kurobanewnavstacktest.widget

import android.view.View

class SlidingPaneLayoutSlideHandler(
  private val initiallyOpen: Boolean
) : SlidingPaneLayoutEx.PanelSlideListener {
  private val listeners = mutableSetOf<SlidingPaneLayoutSlideListener>()
  private var sliding = false
  private var prevOffset: Float = -1f

  fun addListener(listener: SlidingPaneLayoutSlideListener) {
    listeners += listener
  }

  fun removeListener(listener: SlidingPaneLayoutSlideListener) {
    listeners -= listener
  }

  fun clearListeners() {
    listeners.clear()
  }

  override fun onPanelSlide(panel: View, slideOffset: Float) {
    if (!sliding) {
      listeners.forEach { it.onSlidingStarted(initiallyOpen) }
      sliding = true
    }

    if (slideOffset > 0.01 || slideOffset < 0.99) {
      if (Math.abs(slideOffset - prevOffset) < OFFSET_STEP) {
        return
      }
    }

    prevOffset = slideOffset
    listeners.forEach { it.onSliding(slideOffset) }
  }

  override fun onPanelOpened(panel: View) {
    if (prevOffset < 1f) {
      listeners.forEach { it.onSliding(1f) }
    }

    sliding = false
    prevOffset = -1f

    listeners.forEach { it.onSlidingEnded(true) }
  }

  override fun onPanelClosed(panel: View) {
    if (prevOffset > 0f) {
      listeners.forEach { it.onSliding(0f) }
    }

    sliding = false
    prevOffset = -1f

    listeners.forEach { it.onSlidingEnded(false) }
  }

  interface SlidingPaneLayoutSlideListener {
    fun onSlidingStarted(wasOpen: Boolean)
    fun onSliding(offset: Float)
    fun onSlidingEnded(becameOpen: Boolean)
  }

  companion object {
    private const val OFFSET_STEP = 0.03
  }
}