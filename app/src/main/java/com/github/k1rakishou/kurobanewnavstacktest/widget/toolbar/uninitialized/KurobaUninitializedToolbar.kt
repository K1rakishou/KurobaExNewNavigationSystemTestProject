package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.uninitialized

import android.annotation.SuppressLint
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarDelegateContract
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateClass

@SuppressLint("ViewConstructor")
class KurobaUninitializedToolbar(
  context: Context,
  private val toolbarType: KurobaToolbarType
) : ConstraintLayout(context), KurobaToolbarDelegateContract<KurobaUninitializedToolbarState> {

  override val parentToolbarType: KurobaToolbarType
    get() = toolbarType
  override val toolbarStateClass: ToolbarStateClass
    get() = ToolbarStateClass.Uninitialized

  override fun applyStateToUi(toolbarState: KurobaUninitializedToolbarState) {
    throw RuntimeException("Must not be called")
  }

}