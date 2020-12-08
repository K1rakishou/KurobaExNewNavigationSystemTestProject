package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.os.Looper
import java.lang.RuntimeException

object BackgroundUtils {
  private fun isMainThread(): Boolean {
    return Thread.currentThread() === Looper.getMainLooper().thread
  }

  fun ensureMainThread() {
    if (isMainThread()) {
      return
    }

    throw RuntimeException("Cannot be executed on a background thread!")
  }

  fun ensureBackgroundThread() {
    if (!isMainThread()) {
      return
    }

    throw RuntimeException("Cannot be executed on the main thread!")
  }

}