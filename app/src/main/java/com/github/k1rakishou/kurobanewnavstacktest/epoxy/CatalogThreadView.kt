package com.github.k1rakishou.kurobanewnavstacktest.epoxy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnThrottlingClickListener
import com.google.android.material.textview.MaterialTextView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class CatalogThreadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val rootView: ConstraintLayout
  private val imageView: View
  private val textView: MaterialTextView

  init {
    inflate(context, R.layout.epoxy_catalog_thread_view, this)

    rootView = findViewById(R.id.catalog_thread_root)
    imageView = findViewById(R.id.catalog_thread_image)
    textView = findViewById(R.id.catalog_thread_text)
  }

  @ModelProp
  fun setColor(color: Int) {
    imageView.setBackgroundColor(color)
  }

  @ModelProp
  fun setComment(text: String) {
    textView.text = text
  }

  @CallbackProp
  fun clickListener(func: (() -> Unit)?) {
    if (func == null) {
      rootView.setOnThrottlingClickListener(null)
    } else {
      rootView.setOnThrottlingClickListener { func.invoke() }
    }
  }

  @CallbackProp
  fun imageClickListener(func: (() -> Unit)?) {
    if (func == null) {
      imageView.setOnThrottlingClickListener(null)
    } else {
      imageView.setOnThrottlingClickListener { func.invoke() }
    }
  }

}