package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R

class KurobaBottomHiddenPanel(
  context: Context,
) : ConstraintLayout(context, null, 0) {

  init {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_hidden_panel, this, true)
  }

  fun getCurrentHeight(): Int {
    return 0
  }

}