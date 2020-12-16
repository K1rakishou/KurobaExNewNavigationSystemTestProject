package com.github.k1rakishou.kurobanewnavstacktest.core

class GlobalWindowInsetsManager {
  var isKeyboardOpened = false
    private set
  var keyboardHeight = 0
    private set

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