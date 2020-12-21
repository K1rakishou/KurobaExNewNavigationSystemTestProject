package com.github.k1rakishou.kurobanewnavstacktest.widget.animations

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.widget.FrameLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.k1rakishou.kurobanewnavstacktest.core.base.SimpleKurobaAnimation
import com.github.k1rakishou.kurobanewnavstacktest.utils.resumeIfActive
import com.github.k1rakishou.kurobanewnavstacktest.utils.value
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomReplyPanel
import kotlinx.coroutines.suspendCancellableCoroutine

class ReplyPanelSizeChangeAnimation : SimpleKurobaAnimation() {

  suspend fun sizeChangeAnimation(
    parentPanel: KurobaBottomPanel,
    kurobaBottomReplyPanel: KurobaBottomReplyPanel,
    prevHeight: Int,
    newHeight: Int,
    lastInsetBottom: Int,
    updateParentPanelHeightFunc: suspend () -> Unit,
    onBeforePanelBecomesVisibleFunc: suspend () -> Unit
  ) {
    endAnimation()

    val prevScale = kurobaBottomReplyPanel.scaleY
    val newScale = when {
      newHeight > prevHeight -> (newHeight / prevHeight.coerceAtLeast(1)).toFloat()
      newHeight < prevHeight -> (prevHeight / newHeight.coerceAtLeast(1)).toFloat()
      else -> 1f
    }

    // TODO(KurobaEx): there is a little bug in either scale or translation animation which leads
    //  to the bottom part of the panel to appear higher than it should be when expand animating is
    //  running. Probably newScale is calculated without accounting for the bottom inset (lastInsetBottom).
    val prevPivotY = kurobaBottomReplyPanel.pivotY
    val prevTranslationY = kurobaBottomReplyPanel.translationY
    var newTranslationY = prevTranslationY - (newHeight - prevHeight)
    
    if (newHeight > prevHeight) {
      newTranslationY += lastInsetBottom
    } else {
      newTranslationY -= lastInsetBottom
    }

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(prevTranslationY, newTranslationY).apply {
        addUpdateListener { animator ->
          val animatedTranslationY = animator.value<Float>()
          parentPanel.translationY = animatedTranslationY
        }
      }

      val scaleAnimation = ValueAnimator.ofFloat(prevScale, newScale).apply {
        addUpdateListener { animator ->
          parentPanel.scaleY = animator.value()
        }
      }

      val boundsChangeAnimatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, scaleAnimation)
        duration = BOUNDS_CHANGE_ANIMATION_DURATION
        doOnStart {
          parentPanel.pivotY = 0f
        }
      }

      fun onAnimationEnd() {
        onAnimationCompleted()
        continuation.resumeIfActive(Unit)
      }

      animatorSet = AnimatorSet().apply {
        play(boundsChangeAnimatorSet)

        interpolator = INTERPOLATOR
        doOnCancel { onAnimationEnd() }
        doOnEnd { onAnimationEnd() }

        start()
      }

      continuation.invokeOnCancellation {
        onAnimationEnd()
      }
    }

    parentPanel.scaleY = prevScale
    parentPanel.pivotY = prevPivotY
    parentPanel.translationY = prevTranslationY

    kurobaBottomReplyPanel.updateLayoutParams<FrameLayout.LayoutParams> {
      height = newHeight
    }

    updateParentPanelHeightFunc()
    onBeforePanelBecomesVisibleFunc()
  }

  companion object {
    private val INTERPOLATOR = FastOutSlowInInterpolator()

    private const val BOUNDS_CHANGE_ANIMATION_DURATION = 175L
  }

}