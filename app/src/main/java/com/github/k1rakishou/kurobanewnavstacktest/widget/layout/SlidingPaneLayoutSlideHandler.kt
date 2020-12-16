package com.github.k1rakishou.kurobanewnavstacktest.widget.layout

import android.view.View

class SlidingPaneLayoutSlideHandler(
  private val initiallyOpen: Boolean
) : SlidingPaneLayoutEx.PanelSlideListener {
  private val listeners = mutableSetOf<SlidingPaneLayoutSlideListener>()
  private var sliding = false
  private var prevOffset: Float = -1f
  private var currentlyOpen = initiallyOpen

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
      listeners.forEach { it.onSlidingStarted(currentlyOpen) }
      sliding = true
    }

    if (slideOffset > 0.01 || slideOffset < 0.99) {
      if (Math.abs(slideOffset - prevOffset) < OFFSET_MIN_STEP) {
        // To avoid updating listeners too many times which may lead to stuff being redrawn faster
        // than needed which may lead to lags
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

    currentlyOpen = true
    sliding = false
    prevOffset = -1f

    listeners.forEach { it.onSlidingEnded(true) }
  }

  override fun onPanelClosed(panel: View) {
    if (prevOffset > 0f) {
      listeners.forEach { it.onSliding(0f) }
    }

    currentlyOpen = false
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
    private const val OFFSET_MIN_STEP = 1f / 30f
  }
}