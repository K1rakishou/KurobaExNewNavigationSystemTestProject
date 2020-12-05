package com.github.k1rakishou.kurobanewnavstacktest.controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.BaseController
import com.github.k1rakishou.kurobanewnavstacktest.base.ControllerTag
import com.github.k1rakishou.kurobanewnavstacktest.data.PostDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.repository.ChanRepository
import com.github.k1rakishou.kurobanewnavstacktest.utils.FullScreenUtils.hideSystemUi
import com.github.k1rakishou.kurobanewnavstacktest.utils.FullScreenUtils.isSystemUiHidden
import com.github.k1rakishou.kurobanewnavstacktest.utils.FullScreenUtils.showSystemUi
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants

class ImageViewController(args: Bundle? = null) : BaseController(args) {
  private val chanRepository = ChanRepository
  private lateinit var imageView: AppCompatImageView

  private val tapDetector by lazy { GestureDetector(currentContext(), GestureTap { onTap() }) }

  private fun onTap() {
    if (currentActivity().window.isSystemUiHidden()) {
      currentActivity().window.showSystemUi()
    } else {
      currentActivity().window.hideSystemUi()
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun instantiateView(
      inflater: LayoutInflater,
      container: ViewGroup,
      savedViewState: Bundle?
  ): View {
    return inflater.inflateView(R.layout.controller_image_view, container) {
      imageView = findViewById(R.id.image_view)

      setOnTouchListener { v, event ->
        tapDetector.onTouchEvent(event)
        return@setOnTouchListener true
      }
      imageView.setOnTouchListener { v, event ->
        tapDetector.onTouchEvent(event)
        return@setOnTouchListener true
      }

      val postDescriptor =
        args.getParcelable<PostDescriptor>(ViewStateConstants.ImageViewController.postDescriptor)

      checkNotNull(postDescriptor) { "postDescriptor is null" }

      launch {
        val post = chanRepository.getThreadPosts(postDescriptor.threadDescriptor)
          .firstOrNull { post -> post.postDescriptor == postDescriptor }

        if (post != null) {
          imageView.setBackgroundColor(post.color)
        }
      }
    }
  }

  override fun getControllerTag(): ControllerTag = CONTROLLER_TAG


  internal class GestureTap(
      private val onTap: () -> Unit
  ) : GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
      onTap.invoke()
      return true
    }

  }

  companion object {
    val CONTROLLER_TAG = ControllerTag("ImageViewController")

    fun withPostDescriptor(postDescriptor: PostDescriptor): ImageViewController {
      val bundle = Bundle()
      bundle.putParcelable(ViewStateConstants.ImageViewController.postDescriptor, postDescriptor)

      return ImageViewController(bundle)
    }
  }
}