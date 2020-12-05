package com.github.k1rakishou.kurobanewnavstacktest.epoxy

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.github.k1rakishou.kurobanewnavstacktest.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ThreadPostView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val clickableView: FrameLayout
  private val rootView: ConstraintLayout
  private val imageView: View
  private val textView: TextView

  init {
    inflate(context, R.layout.epoxy_thread_post_view, this)

    clickableView = findViewById(R.id.thread_post_clickable_view)
    rootView = findViewById(R.id.thread_post_image_root)
    imageView = findViewById(R.id.thread_post_image)
    textView = findViewById(R.id.thread_post_text)
  }

  @ModelProp
  fun setColor(color: Int) {
    imageView.setBackgroundColor(color)
  }

  @ModelProp
  fun setPostSelected(selected: Boolean) {
    if (selected) {
      rootView.setBackgroundColor(Color.LTGRAY)
    } else {
      rootView.setBackgroundColor(0xEEEEEE.toInt())
    }
  }

  @ModelProp
  fun setComment(text: String) {
    textView.text = text
  }

  @CallbackProp
  fun clickListener(func: (() -> Unit)?) {
    if (func == null) {
      clickableView.setOnClickListener(null)
    } else {
      clickableView.setOnClickListener { func.invoke() }
    }
  }

  @CallbackProp
  fun imageClickListener(func: (() -> Unit)?) {
    if (func == null) {
      imageView.setOnClickListener(null)
    } else {
      imageView.setOnClickListener { func.invoke() }
    }
  }

}