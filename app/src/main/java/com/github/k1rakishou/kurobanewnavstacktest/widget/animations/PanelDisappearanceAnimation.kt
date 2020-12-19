package com.github.k1rakishou.kurobanewnavstacktest.widget.animations

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.k1rakishou.kurobanewnavstacktest.core.base.SimpleKurobaAnimation
import com.github.k1rakishou.kurobanewnavstacktest.utils.resumeIfActive
import com.github.k1rakishou.kurobanewnavstacktest.utils.setAlphaFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setVisibilityFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.value
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import kotlinx.coroutines.suspendCancellableCoroutine

class PanelDisappearanceAnimation : SimpleKurobaAnimation() {

  suspend fun disappearanceAnimation(
    parentPanel: KurobaBottomPanel,
    panelContainer: FrameLayout,
    lastInsetBottom: Int,
    attachedFab: KurobaFloatingActionButton?,
    prevTranslationY: Float,
    prevHeight: Int,
    newHeight: Int
  ) {
    require(newHeight < prevHeight) { "Bad height: prevHeight=$prevHeight, newHeight=$newHeight" }
    endAnimation()

    val newTranslationY = prevTranslationY + (prevHeight - newHeight)
    val initialFabTranslationY = attachedFab?.translationY

    suspendCancellableCoroutine<Unit> { continuation ->
      val translationAnimation = ValueAnimator.ofFloat(prevTranslationY, newTranslationY).apply {
        addUpdateListener { animator ->
          parentPanel.translationY = animator.value()

          attachedFab?.let { fab ->
            initialFabTranslationY?.let { initialY ->
              val deltaTranslationY = animator.value<Float>()
              fab.translationY = initialY + deltaTranslationY
            }
          }
        }
      }

      val alphaAnimation = ValueAnimator.ofFloat(1f, 0f).apply {
        addUpdateListener { animator ->
          parentPanel.setAlphaFast(animator.value())
        }
      }

      fun onAnimationEnd() {
        onAnimationCompleted()
        continuation.resumeIfActive(Unit)
      }

      animatorSet = AnimatorSet().apply {
        playTogether(translationAnimation, alphaAnimation)

        interpolator = INTERPOLATOR
        duration = ANIMATION_DURATION
        doOnCancel { onAnimationEnd() }

        doOnEnd {
          parentPanel.translationY = prevTranslationY

          panelContainer.updateLayoutParams<FrameLayout.LayoutParams> { height = 0 }
          panelContainer.updatePadding(bottom = lastInsetBottom)
          parentPanel.setVisibilityFast(View.INVISIBLE)

          onAnimationEnd()
        }

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