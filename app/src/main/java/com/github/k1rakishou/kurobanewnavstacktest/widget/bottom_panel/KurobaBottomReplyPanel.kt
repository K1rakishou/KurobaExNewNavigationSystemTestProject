package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.google.android.material.textfield.TextInputLayout

class KurobaBottomReplyPanel(
  context: Context,
) : ConstraintLayout(context, null, 0) {
  private val replyInputLayout: TextInputLayout
  private val replyInputEditText: AppCompatEditText

  init {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_reply_panel, this, true)

    replyInputLayout = findViewById(R.id.reply_input_layout)
    replyInputEditText = findViewById(R.id.reply_input_edit_text)
  }

  fun getCurrentHeight(): Int {
    return context.resources.getDimension(R.dimen.bottom_reply_panel_height).toInt()
  }

}