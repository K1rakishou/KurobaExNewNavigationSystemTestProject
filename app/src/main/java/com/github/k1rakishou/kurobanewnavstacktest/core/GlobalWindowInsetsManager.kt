package com.github.k1rakishou.kurobanewnavstacktest.core

import androidx.core.view.WindowInsetsCompat

class GlobalWindowInsetsManager {
  var topInset: Int = 0
    private set
  var bottomInset: Int = 0
    private set
  var rightInset: Int = 0
    private set
  var leftInset: Int = 0
    private set

  var isKeyboardOpened = false
    private set
  var keyboardHeight = 0
    private set

  fun updateInsets(insets: WindowInsetsCompat) {
    topInset = insets.systemWindowInsetTop
    bottomInset = insets.systemWindowInsetBottom
    rightInset = insets.systemWindowInsetRight
    leftInset = insets.systemWindowInsetLeft
  }

  fun updateIsKeyboardOpened(opened: Boolean) {
    if (isKeyboardOpened == opened) {
      return
    }

    isKeyboardOpened = opened
  }

  fun updateKeyboardHeight(height: Int) {
    keyboardHeight = height.coerceAtLeast(0)
  }

}