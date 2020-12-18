package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.doIgnoringTextWatcher
import com.github.k1rakishou.kurobanewnavstacktest.utils.requestLayoutAndAwait
import com.github.k1rakishou.kurobanewnavstacktest.utils.setEnabledFast

@SuppressLint("ViewConstructor")
class KurobaBottomReplyPanel(
  context: Context,
  initialControllerType: ControllerType,
  private val viewModel: KurobaBottomPanelViewModel
) : ConstraintLayout(context, null, 0), ChildPanelContract {
  private var controllerType = initialControllerType

  private val replyPanelRoot: ConstraintLayout
  private val replyInputEditText: AppCompatEditText

  private val viewState: KurobaBottomReplyPanelViewState
    get() = viewModel.getBottomPanelState(controllerType).bottomReplyPanelState

  private val textWatcher: TextWatcher

  init {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_reply_panel, this, true)
    replyPanelRoot = findViewById(R.id.reply_panel_root)
    replyInputEditText = findViewById(R.id.reply_input_edit_text)

    textWatcher = replyInputEditText.doOnTextChanged { text, _, _, _ ->
      viewState.text = text?.toString()
      viewState.selectionStart = replyInputEditText.selectionStart
      viewState.selectionEnd = replyInputEditText.selectionEnd
    }
  }

  override fun getCurrentHeight(): Int {
    return context.resources.getDimension(R.dimen.bottom_reply_panel_height).toInt()
  }

  override fun getBackgroundColor(): Int {
    return context.resources.getColor(R.color.backColorDark)
  }

  override fun enableOrDisableControls(enable: Boolean) {
    replyInputEditText.setEnabledFast(enable)
  }

  override fun saveState(bottomPanelViewState: KurobaBottomPanelViewState) {
  }

  override fun restoreState(bottomPanelViewState: KurobaBottomPanelViewState) {
    val bottomReplyPanelState = bottomPanelViewState.bottomReplyPanelState

    replyInputEditText.doIgnoringTextWatcher(textWatcher) {
      setText(bottomReplyPanelState.text ?: "")

      val selectionStart = bottomReplyPanelState.selectionStart
      val selectionEnd = bottomReplyPanelState.selectionEnd

      setSelectionSafe(selectionStart, selectionEnd)
    }
  }

  override fun updateCurrentControllerType(controllerType: ControllerType) {
    this.controllerType = controllerType
  }

  private fun EditText.setSelectionSafe(selectionStart: Int?, selectionEnd: Int?) {
    if (selectionStart != null && selectionEnd != null) {
      setSelection(selectionStart, selectionEnd)
    } else if (selectionStart != null) {
      setSelection(selectionStart)
    }
  }

  override suspend fun updateHeight(parentHeight: Int) {
    replyPanelRoot.updateLayoutParams<LayoutParams> { height = getCurrentHeight() }
    replyPanelRoot.requestLayoutAndAwait()
  }

  companion object {
    private const val TAG = "KurobaBottomReplyPanel"
  }

}