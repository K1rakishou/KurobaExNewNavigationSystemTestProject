package com.github.k1rakishou.kurobanewnavstacktest.widget.animations

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.widget.FrameLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.k1rakishou.kurobanewnavstacktest.core.base.SimpleKurobaAnimation
import com.github.k1rakishou.kurobanewnavstacktest.utils.getChildAtOrNull
import com.github.k1rakishou.kurobanewnavstacktest.utils.resumeIfActive
import com.github.k1rakishou.kurobanewnavstacktest.utils.setAlphaFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.value
import kotlinx.coroutines.suspendCancellableCoroutine

class PanelRemoveAnimation : SimpleKurobaAnimation() {

  suspend fun removeOldChildPanelWithAnimation(
    panelContainer: FrameLayout,
    destroyPanelFunc: () -> Unit
  ) {
    val childPanel = panelContainer.getChildAtOrNull(0)
      ?: return

    endAnimation()

    suspendCancellableCoroutine<Unit> { continuation ->
      val alphaAnimation = ValueAnimator.ofFloat(1f, 0f).apply {
        duration = ALPHA_ANIMATION_DURATION
        addUpdateListener { animator ->
          childPanel.setAlphaFast(animator.value())
        }
      }

      fun onAnimationEnd() {
        destroyPanelFunc()
        onAnimationCompleted()
        continuation.resumeIfActive(Unit)
      }

      animatorSet = AnimatorSet().apply {
        play(alphaAnimation)

        interpolator = INTERPOLATOR
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
    private const val ALPHA_ANIMATION_DURATION = 75L
    private val INTERPOLATOR = FastOutSlowInInterpolator()
  }

}