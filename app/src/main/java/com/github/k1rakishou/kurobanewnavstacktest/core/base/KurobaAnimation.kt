package com.github.k1rakishou.kurobanewnavstacktest.core.base

import android.animation.AnimatorSet
import com.github.k1rakishou.kurobanewnavstacktest.utils.endAndAwait

interface KurobaAnimation {
  fun endAnimation()
  suspend fun endAnimationAndAwait()
  fun onAnimationCompleted()
  fun isRunning(): Boolean
}

abstract class SimpleKurobaAnimation : KurobaAnimation {
  protected var animatorSet: AnimatorSet? = null

  override fun endAnimation() {
    if (animatorSet != null) {
      animatorSet?.end()
      animatorSet = null
    }
  }

  override suspend fun endAnimationAndAwait() {
    animatorSet?.endAndAwait()
  }

  override fun onAnimationCompleted() {
    animatorSet = null
  }

  override fun isRunning(): Boolean {
    return animatorSet != null
  }
}