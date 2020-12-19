package com.github.k1rakishou.kurobanewnavstacktest.core.base

import android.animation.AnimatorSet

interface KurobaAnimation {
  fun endAnimation()
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

  override fun onAnimationCompleted() {
    animatorSet = null
  }

  override fun isRunning(): Boolean {
    return animatorSet != null
  }
}