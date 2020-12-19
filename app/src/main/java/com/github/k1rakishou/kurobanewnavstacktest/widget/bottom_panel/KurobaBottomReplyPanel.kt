package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.*

@SuppressLint("ViewConstructor")
class KurobaBottomReplyPanel(
  context: Context,
  initialControllerType: ControllerType,
  private val totalAvailableHeight: Int,
  private val viewModel: KurobaBottomPanelViewModel,
  private val callbacks: KurobaBottomPanelCallbacks
) : ConstraintLayout(context, null, 0), ChildPanelContract {
  private var controllerType = initialControllerType

  private lateinit var replyPanelRoot: ConstraintLayout
  private lateinit var replyInputEditText: AppCompatEditText
  private lateinit var replyPanelExpandCollapseButton: TextView
  private lateinit var textWatcher: TextWatcher

  private val viewState: KurobaBottomReplyPanelViewState
    get() = viewModel.getBottomPanelState(controllerType).bottomReplyPanelState

  private val scope = KurobaCoroutineScope()

  init {
    initializeView(context)
  }

  private fun initializeView(context: Context) {
    if (viewState.expanded) {
      LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_reply_panel_expanded, this, true)
    } else {
      LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_reply_panel_collapsed, this, true)
    }

    replyPanelRoot = findViewById(R.id.reply_panel_root)
    replyInputEditText = findViewById(R.id.reply_input_edit_text)
    replyPanelExpandCollapseButton = findViewById(R.id.reply_panel_expand_collapse_button)

    replyPanelExpandCollapseButton.setOnThrottlingClickListener {
      scope.launch { updateExpandedCollapsedState(viewState.expanded.not()) }
    }

    setOnApplyWindowInsetsListenerAndDoRequest { v, insets ->
      if (viewState.expanded) {
        updatePadding(top = insets.systemWindowInsetTop)
      } else {
        updatePadding(top = 0)
      }

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }

    if (::textWatcher.isInitialized) {
      replyInputEditText.removeTextChangedListener(textWatcher)
    }

    textWatcher = replyInputEditText.doOnTextChanged { text, _, _, _ ->
      viewState.text = text?.toString()
      viewState.selectionStart = replyInputEditText.selectionStart
      viewState.selectionEnd = replyInputEditText.selectionEnd
    }
  }

  private suspend fun updateExpandedCollapsedState(expanded: Boolean) {
    this.setVisibilityFast(View.INVISIBLE)

    viewState.expanded = expanded
    callbacks.updateParentPanelHeight(getCurrentHeight())

    removeAllViews()
    initializeView(context)

    this.setVisibilityFast(View.VISIBLE)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    updateLayoutParams<FrameLayout.LayoutParams> { height = getCurrentHeight() }
  }

  override fun getCurrentHeight(): Int {
    return if (viewState.expanded) {
      totalAvailableHeight
    } else {
      context.resources.getDimension(R.dimen.bottom_reply_panel_height).toInt()
    }
  }

  override fun getBackgroundColor(): Int {
    return context.resources.getColor(R.color.backColorDark)
  }

  override fun enableOrDisableControls(enable: Boolean) {
    replyInputEditText.setEnabledFast(enable)
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

  override fun handleBack(): Boolean {
    if (viewState.expanded) {
      scope.launch { updateExpandedCollapsedState(expanded = false) }

      return true
    }

    return false
  }

  override fun onDestroy() {
    scope.cancelChildren()
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

  interface KurobaBottomPanelCallbacks {
    suspend fun updateParentPanelHeight(newHeight: Int)
  }

  companion object {
    private const val TAG = "KurobaBottomReplyPanel"
  }

}