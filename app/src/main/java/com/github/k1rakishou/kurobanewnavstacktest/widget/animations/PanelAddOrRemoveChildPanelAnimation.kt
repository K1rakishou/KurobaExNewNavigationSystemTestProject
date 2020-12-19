package com.github.k1rakishou.kurobanewnavstacktest.widget.animations

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.k1rakishou.kurobanewnavstacktest.core.base.SimpleKurobaAnimation
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.ChildPanelContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel.KurobaBottomPanel
import com.github.k1rakishou.kurobanewnavstacktest.widget.fab.KurobaFloatingActionButton
import kotlinx.coroutines.suspendCancellableCoroutine

class PanelAddOrRemoveChildPanelAnimation : SimpleKurobaAnimation() {

  suspend fun addNewChildPanelWithAnimation(
    parentPanel: KurobaBottomPanel,
    panelContainer: FrameLayout,
    attachedFab: KurobaFloatingActionButton?,
    childPanel: View,
    lastInsetBottom: Int,
    prevColor: Int,
    newColor: Int,
    prevHeight: Int,
    newHeight: Int,
    doBeforeViewVisible: suspend (View) -> Unit
  ) {
    endAnimation()
    childPanel as ChildPanelContract

    val prevScale = panelContainer.scaleY
    val newScale = when {
      newHeight > prevHeight -> (newHeight / prevHeight).toFloat()
      newHeight < prevHeight -> (prevHeight / newHeight).toFloat()
      else -> 1f
    }

    val prevPivotY = panelContainer.pivotY
    val prevTranslationY = panelContainer.translationY
    val newTranslationY = prevTranslationY - (newHeight - prevHeight)
    val initialFabTranslationY = attachedFab?.translationY

    panelContainer.setVisibilityFast(View.VISIBLE)
    parentPanel.setAlphaFast(1f)
    parentPanel.setVisibilityFast(View.VISIBLE)
    parentPanel.setBackgroundColorFast(newColor)

    suspendCancellableCoroutine<Unit> { continuation ->
      val colorAnimation = ValueAnimator.ofArgb(prevColor, newColor).apply {
        addUpdateListener { animator ->
          panelContainer.setBackgroundColorFast(animator.value())
          childPanel.setBackgroundColorFast(animator.value())
        }
      }

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

      val scaleAnimation = ValueAnimator.ofFloat(prevScale, newScale).apply {
        addUpdateListener { animator ->
          parentPanel.scaleY = animator.value()
        }
      }

      val boundsChangeAnimatorSet = AnimatorSet().apply {
        playTogether(colorAnimation, translationAnimation, scaleAnimation)
        duration = BOUNDS_CHANGE_ANIMATION_DURATION
        doOnStart {
          parentPanel.pivotY = 0f
        }
        doOnEnd {
          parentPanel.scaleY = prevScale
          parentPanel.pivotY = prevPivotY
          parentPanel.translationY = prevTranslationY

          panelContainer.updateLayoutParams<FrameLayout.LayoutParams> {
            height = childPanel.getCurrentHeight() + lastInsetBottom
          }
          panelContainer.updatePadding(bottom = lastInsetBottom)
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

    doBeforeViewVisible(childPanel)

    suspendCancellableCoroutine<Unit> { continuation ->
      val alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = ALPHA_ANIMATION_DURATION
        addUpdateListener { animator ->
          childPanel.setAlphaFast(animator.value())
        }
      }

      fun onAnimationEnd() {
        onAnimationCompleted()
        continuation.resumeIfActive(Unit)
      }

      animatorSet = AnimatorSet().apply {
        play(alphaAnimation)

        interpolator = INTERPOLATOR
        doOnStart {
          childPanel.setAlphaFast(0f)
          childPanel.setVisibilityFast(View.VISIBLE)
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
    private val INTERPOLATOR = FastOutSlowInInterpolator()

    private const val BOUNDS_CHANGE_ANIMATION_DURATION = 175L
    private const val ALPHA_ANIMATION_DURATION = 75L
  }

}