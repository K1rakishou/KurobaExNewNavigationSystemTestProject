package com.github.k1rakishou.kurobanewnavstacktest.viewcontroller

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import androidx.core.animation.addListener
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.controller.base.CollapsableView
import com.github.k1rakishou.kurobanewnavstacktest.utils.ChanSettings
import com.github.k1rakishou.kurobanewnavstacktest.utils.dp
import kotlin.math.abs

class CollapsingViewController(
  val context: Context,
  private val viewScreenAttachSide: ViewScreenAttachSide
) {
  private var viewRef: CollapsableView? = null
  private var animationState = ANIMATION_IDLE
  private var locked: Boolean = false

  var isShown: Boolean = true
    private set

  private val animatorSet = AnimatorSet()
  private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

  private val scrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        runScrollAnimation()
      }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      if (dy == 0) {
        return
      }

      manualScroll(dy)
    }
  }

  init {
    locked = ChanSettings.collapsibleViewsAlwaysLocked(context)
  }

  fun lockUnlock(lock: Boolean, animate: Boolean) {
    if (lock) {
      viewRef?.let { view ->
        playTranslationAnimation(
          show = true,
          animate = animate,
          from = view.translationY(),
          to = 0f
        )

        locked = lock
      }

      return
    }

    if (!ChanSettings.collapsibleViewsAlwaysLocked(context)) {
      locked = lock
    }
  }

  fun show(animate: Boolean) {
    animatorSet.end()

    viewRef?.let { view ->
      playTranslationAnimation(
        show = true,
        animate = animate,
        from = view.translationY(),
        to = 0f
      )
    }
  }

  fun hide(lockHidden: Boolean, animate: Boolean) {
    animatorSet.end()

    viewRef?.let { view ->
      when (viewScreenAttachSide) {
        ViewScreenAttachSide.Top -> {
          playTranslationAnimation(
            show = false,
            animate = animate,
            from = view.translationY(),
            to = -view.height()
          )
        }
        ViewScreenAttachSide.Bottom -> {
          playTranslationAnimation(
            show = false,
            animate = animate,
            from = view.translationY(),
            to = -view.height()
          )
        }
      }

      locked = lockHidden
    }
  }

  fun attach(view: CollapsableView, recyclerView: RecyclerView) {
    viewRef = view

    setup()
    recyclerView.addOnScrollListener(scrollListener)
  }

  fun detach(recyclerView: RecyclerView) {
    reset()
    recyclerView.removeOnScrollListener(scrollListener)
  }

  private fun manualScroll(dy: Int) {
    if (locked) {
      return
    }

    if (animationState != ANIMATION_IDLE
      && animationState != ANIMATION_WANT_SHOW
      && animationState != ANIMATION_WANT_HIDE
    ) {
      return
    }

    val view = viewRef
      ?: return

    if (dy > 0) {
      animationState = ANIMATION_WANT_HIDE
    } else if (dy < 0) {
      animationState = ANIMATION_WANT_SHOW
    }

    var newTranslationY = 0f

    if (viewScreenAttachSide == ViewScreenAttachSide.Bottom) {
      newTranslationY = view.translationY() + dy

      if (newTranslationY < 0f) {
        newTranslationY = 0f
      } else if (newTranslationY > view.height()) {
        newTranslationY = view.height()
      }
    } else {
      newTranslationY = view.translationY() - dy

      if (newTranslationY < -view.height()) {
        newTranslationY = -view.height()
      } else if (newTranslationY > 0f) {
        newTranslationY = 0f
      }
    }

    view.translationY(newTranslationY)
  }

  private fun runScrollAnimation() {
    if (locked) {
      return
    }

    if (animationState == ANIMATION_IDLE
      || animationState == ANIMATION_RUNNING_HIDE
      || animationState == ANIMATION_RUNNING_SHOW
    ) {
      return
    }

    val view = viewRef
      ?: return

    val bottomFunc = { view.translationY() > (0f + (view.height() * .25f)) }
    val invBottomFunc = { view.translationY() < ((0f + view.height()) - (view.height() * .25f)) }
    val topFunc = { view.translationY() < ((0f) - (view.height() * .25f)) }
    val invTopFunc = { view.translationY() > ((0f - view.height()) + (view.height() * .25f)) }

    animationState = when (animationState) {
      ANIMATION_WANT_HIDE -> {
        val checkFunc = when (viewScreenAttachSide) {
          ViewScreenAttachSide.Top -> topFunc
          ViewScreenAttachSide.Bottom -> bottomFunc
        }

        // If the view has been scrolled for more than 25% of it's height then we can run the
        // hide animation, otherwise animate back to where it was before
        if (checkFunc.invoke()) {
          ANIMATION_RUNNING_HIDE
        } else {
          ANIMATION_RUNNING_SHOW
        }
      }
      ANIMATION_WANT_SHOW -> {
        val checkFunc = when (viewScreenAttachSide) {
          ViewScreenAttachSide.Top -> invTopFunc
          ViewScreenAttachSide.Bottom -> invBottomFunc
        }

        if (checkFunc.invoke()) {
          ANIMATION_RUNNING_SHOW
        } else {
          ANIMATION_RUNNING_HIDE
        }
      }
      else -> throw IllegalStateException("Bad state: $animationState")
    }

    check(animationState == ANIMATION_RUNNING_SHOW || animationState == ANIMATION_RUNNING_HIDE) {
      "Bad state: $animationState"
    }

    val targetY = if (animationState == ANIMATION_RUNNING_SHOW) {
      0f
    } else {
      when (viewScreenAttachSide) {
        ViewScreenAttachSide.Top -> 0f - view.height()
        ViewScreenAttachSide.Bottom -> 0f + view.height()
      }
    }

    playTranslationAnimation(
      animationState == ANIMATION_RUNNING_SHOW,
      true,
      view.translationY(),
      targetY
    )
  }

  private fun playTranslationAnimation(show: Boolean, animate: Boolean, from: Float, to: Float) {
    if (abs(from - to) < MIN_ANIM_DISTANCE) {
      viewRef?.translationY(to)
      animationState = ANIMATION_IDLE
      isShown = show
      return
    }

    if (animate) {
      val finalizeTranslationY = { viewRef?.translationY(to) }
      val finalizeAnimationState = { animationState = ANIMATION_IDLE }

      val animator = ValueAnimator.ofFloat(from, to).apply {
        removeAllUpdateListeners()
        doOnEnd { finalizeTranslationY() }
        doOnCancel { finalizeTranslationY() }
        addUpdateListener { valueAnimator ->
          viewRef?.translationY(valueAnimator.animatedValue as Float)
        }
      }

      animatorSet.play(animator)
      animatorSet.duration = 150
      animatorSet.interpolator = fastOutSlowInInterpolator
      animatorSet.addListener(
        onCancel = { finalizeAnimationState() },
        onEnd = { finalizeAnimationState() }
      )
      animatorSet.start()
    } else {
      viewRef?.translationY(to)
    }

    isShown = show
  }

  private fun setup() {
    val view = viewRef
      ?: return

    require(view.isLaidOut()) { "View is not laid out" }

    if (view.height() <= 0) {
      return
    }
  }

  private fun reset() {
    animatorSet.end()
    animationState = ANIMATION_IDLE

    viewRef?.translationY(0f)
    viewRef = null

    locked = false
    isShown = true
  }

  companion object {
    private const val ANIMATION_IDLE = 0
    private const val ANIMATION_WANT_SHOW = 1
    private const val ANIMATION_WANT_HIDE = 2
    private const val ANIMATION_RUNNING_SHOW = 3
    private const val ANIMATION_RUNNING_HIDE = 4

    private val MIN_ANIM_DISTANCE = 4.dp
  }
}

enum class ViewScreenAttachSide {
  Top,
  Bottom
}