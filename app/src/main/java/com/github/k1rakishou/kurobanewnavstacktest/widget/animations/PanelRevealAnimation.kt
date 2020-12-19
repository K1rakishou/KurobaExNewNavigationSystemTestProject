package com.github.k1rakishou.kurobanewnavstacktest.widget.animations

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.k1rakishou.kurobanewnavstacktest.core.base.SimpleKurobaAnimation
import com.github.k1rakishou.kurobanewnavstacktest.utils.resumeIfActive
import com.github.k1rakishou.kurobanewnavstacktest.utils.setAlphaFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setVisibilityFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.value
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import kotlinx.coroutines.suspendCancellableCoroutine

class PanelRevealAnimation : SimpleKurobaAnimation() {

  suspend fun bottomNavViewPanelRevealAnimation(
    parentPanel: KurobaBottomPanel,
    childPanel: View,
    startTrY: Float,
    endTrY: Float
  ) {
    endAnimation()

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(startTrY, endTrY).apply {
        addUpdateListener { animator ->
          parentPanel.translationY(animator.value())
        }
      }

      val alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener { animator ->
          parentPanel.setAlphaFast(animator.value())
          childPanel.setAlphaFast(animator.value())
        }
      }

      fun onAnimationEnd() {
        onAnimationCompleted()
        continuation.resumeIfActive(Unit)
      }

      animatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, alphaAnimation)
        duration = ANIMATION_DURATION
        interpolator = INTERPOLATOR
        doOnStart {
          childPanel.setVisibilityFast(View.VISIBLE)
          parentPanel.setVisibilityFast(View.VISIBLE)
          childPanel.setAlphaFast(0f)
          parentPanel.setAlphaFast(0f)
        }
        doOnCancel { onAnimationEnd() }
        doOnEnd { onAnimationEnd() }

        start()
      }

      continuation.invokeOnCancellation {
        onAnimationEnd()
      }
    }
  }

  companion object {
    private const val ANIMATION_DURATION = 250L
    private val INTERPOLATOR = FastOutSlowInInterpolator()
  }

}