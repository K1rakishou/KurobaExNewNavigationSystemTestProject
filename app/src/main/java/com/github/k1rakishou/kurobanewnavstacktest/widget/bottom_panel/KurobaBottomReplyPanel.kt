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
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.core.base.KurobaCoroutineScope
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.data.ReplyAttachmentFile
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.EpoxyAttachNewFileButtonView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.EpoxyAttachNewFileButtonViewModel_
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.epoxyAttachNewFileButtonView
import com.github.k1rakishou.kurobanewnavstacktest.epoxy.epoxyReplyAttachmentFileView
import com.github.k1rakishou.kurobanewnavstacktest.utils.*
import com.github.k1rakishou.kurobanewnavstacktest.widget.animations.ReplyPanelSizeChangeAnimation
import kotlin.random.Random

@SuppressLint("ViewConstructor")
class KurobaBottomReplyPanel(
  context: Context,
  initialControllerType: ControllerType,
  private val parentPanel: KurobaBottomPanel,
  private val availableVerticalSpace: Int,
  private val viewModel: KurobaBottomPanelViewModel,
  private val callbacks: KurobaBottomPanelCallbacks
) : ConstraintLayout(context, null, 0), ChildPanelContract {
  private lateinit var replyPanelRoot: ConstraintLayout
  private lateinit var replyInputEditText: AppCompatEditText
  private lateinit var replyPanelExpandCollapseButton: TextView
  private lateinit var replyAttachmentsRecyclerView: EpoxyRecyclerView
  private lateinit var replyPanelTopPart: ConstraintLayout
  private lateinit var replyPanelBottomPart: ConstraintLayout
  private lateinit var textWatcher: TextWatcher

  private var controllerType = initialControllerType
  private var currentlyExpanded = false
  private var lastInsetBottom = 0

  private val controller = KurobaBottomReplyPanelEpoxyController()
  private val replyPanelSizeChangeAnimation = ReplyPanelSizeChangeAnimation()

  private val viewState: KurobaBottomReplyPanelViewState
    get() = viewModel.getBottomPanelState(controllerType).bottomReplyPanelState

  private val scope = KurobaCoroutineScope()

  override suspend fun initializeView() {
    initializeViewInternal()
    dispatchUpdateViewHeight()
  }

  private fun dispatchUpdateViewHeight() {
    doOnLayout {
      val replyAttachmentsRecyclerViewWidth = when {
        replyAttachmentsRecyclerView.width > 0 -> replyAttachmentsRecyclerView.width
        replyAttachmentsRecyclerView.measuredWidth > 0 -> replyAttachmentsRecyclerView.measuredWidth
        else -> throw IllegalStateException("View is not measured!")
      }

      val replyPanelTopPartHeight = when {
        replyPanelTopPart.height > 0 -> replyPanelTopPart.height
        replyPanelTopPart.measuredHeight > 0 -> replyPanelTopPart.measuredHeight
        else -> throw IllegalStateException("View is not measured!")
      }

      updateLayoutParams<FrameLayout.LayoutParams> { height = getCurrentHeight() }

      updateLayoutManagerAndSpanCount(replyAttachmentsRecyclerViewWidth)
      updateReplyPanelBottomPartHeight(replyPanelTopPartHeight)
    }
  }

  private fun initializeViewInternal() {
    val layoutInflater = LayoutInflater.from(context)

    val view = if (viewState.expanded) {
      layoutInflater.inflate(R.layout.kuroba_bottom_reply_panel_expanded, this, false)
    } else {
      layoutInflater.inflate(R.layout.kuroba_bottom_reply_panel_collapsed, this, false)
    }

    view.visibility = INVISIBLE
    addView(view)

    replyPanelRoot = findViewById(R.id.reply_panel_root)
    replyInputEditText = findViewById(R.id.reply_input_edit_text)
    replyPanelExpandCollapseButton = findViewById(R.id.reply_panel_expand_collapse_button)
    replyPanelTopPart = findViewById(R.id.reply_panel_top_part)
    replyPanelBottomPart = findViewById(R.id.reply_panel_bottom_part)
    replyAttachmentsRecyclerView = findViewById(R.id.reply_attachments_recycler_view)
    replyAttachmentsRecyclerView.setController(controller)

    replyPanelExpandCollapseButton.setOnThrottlingClickListener {
      scope.launch { updateExpandedCollapsedState(viewState.expanded.not()) }
    }

    setOnApplyWindowInsetsListenerAndDoRequest { parentView, insets ->
      lastInsetBottom = insets.systemWindowInsetBottom

      if (viewState.expanded) {
        parentView.updatePadding(top = insets.systemWindowInsetTop)
      } else {
        parentView.updatePadding(top = 0)
      }

      return@setOnApplyWindowInsetsListenerAndDoRequest insets
    }

    replyAttachmentsRecyclerView.setOnApplyWindowInsetsListenerAndDoRequest { recyclerView, insets ->
      lastInsetBottom = insets.systemWindowInsetBottom

      if (viewState.expanded) {
        recyclerView.updatePadding(bottom = insets.systemWindowInsetBottom)
      } else {
        recyclerView.updatePadding(bottom = 0)
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

  override suspend fun onPanelAttachedToParent() {
    val view = getChildAtOrNull(0)
      ?: return

    view.visibility = View.VISIBLE
  }

  private fun updateReplyPanelBottomPartHeight(replyPanelTopPartHeight: Int) {
    if (!viewState.expanded) {
      return
    }

    check(availableVerticalSpace > replyPanelTopPartHeight) {
      "availableVerticalSpace ($availableVerticalSpace) must be greater " +
        "than replyPanelTopPartHeight ($replyPanelTopPartHeight)"
    }

    val replyPanelBottomPartHeight = availableVerticalSpace - replyPanelTopPartHeight
    check(replyPanelBottomPartHeight > 0) { "Bad recyclerHeight: $replyPanelBottomPartHeight" }

    replyPanelBottomPart.updateLayoutParams<ConstraintLayout.LayoutParams> {
      height = replyPanelBottomPartHeight
    }
  }

  private fun updateLayoutManagerAndSpanCount(replyAttachmentsRecyclerViewWidth: Int) {
    check(replyAttachmentsRecyclerViewWidth > 0) {
      "Bad replyAttachmentsRecyclerViewWidth: $replyAttachmentsRecyclerViewWidth"
    }

    val attachNewFileButtonWidth = if (viewState.expanded) {
      context.resources.getDimension(R.dimen.attach_new_file_button_expanded_width)
    } else {
      context.resources.getDimension(R.dimen.attach_new_file_button_collapsed_width)
    }

    val spanCount = (replyAttachmentsRecyclerViewWidth / attachNewFileButtonWidth.toInt())
      .coerceAtLeast(2)

    val prevSpanCount = (replyAttachmentsRecyclerView.layoutManager as? GridLayoutManager)
      ?.spanCount
      ?: -1

    if (prevSpanCount == spanCount) {
      return
    }

    controller.currentSpanCount = spanCount

    val layoutManager = GridLayoutManager(context, spanCount)
    layoutManager.spanSizeLookup = controller.spanSizeLookup
    replyAttachmentsRecyclerView.layoutManager = layoutManager

    redrawAttachmentFiles()
  }

  private fun redrawAttachmentFiles() {
    controller.callback = redraw@ {
      if (viewState.replyAttachments.isEmpty()) {
        epoxyAttachNewFileButtonView {
          id("epoxy_new_attachment_button_empty")
          size(EpoxyAttachNewFileButtonView.Size(viewState.expanded, true))
          onClickListener { attachNewReplyFile() }
        }
        return@redraw
      }

      viewState.replyAttachments.forEach { attachment ->
        epoxyReplyAttachmentFileView {
          id("epoxy_reply_attachment_file_${attachment.id}")
          expandedCollapsedMode(viewState.expanded)
          color(attachment.color)
        }
      }

      epoxyAttachNewFileButtonView {
        id("epoxy_new_attachment_button_not_empty")
        size(EpoxyAttachNewFileButtonView.Size(viewState.expanded, false))
        onClickListener { attachNewReplyFile() }
      }
    }

    controller.requestModelBuild()
  }

  private fun attachNewReplyFile() {
    val nextId = viewState.replyAttachments.lastOrNull()?.id?.plus(1) ?: 0
    viewState.replyAttachments += ReplyAttachmentFile(nextId, random.nextInt())

    redrawAttachmentFiles()
  }

  private suspend fun updateExpandedCollapsedState(expanded: Boolean) {
    this.visibility = View.INVISIBLE

    val prevHeight = getCurrentHeight()
    viewState.expanded = expanded
    val currentHeight = getCurrentHeight()

    replyPanelSizeChangeAnimation.sizeChangeAnimation(
      parentPanel,
      this,
      prevHeight,
      currentHeight,
      lastInsetBottom,
      updateParentPanelHeightFunc = {
        callbacks.updateParentPanelHeight(currentHeight)
      },
      onBeforePanelBecomesVisibleFunc = {
        removeAllViews()
        initializeViewInternal()

        restoreState(viewModel.getBottomPanelState(controllerType))
      }
    )

    onPanelAttachedToParent()
    dispatchUpdateViewHeight()

    this.visibility = View.VISIBLE
    this.currentlyExpanded = expanded
  }

  override fun getCurrentHeight(): Int {
    return if (viewState.expanded) {
      availableVerticalSpace
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
    if (replyPanelSizeChangeAnimation.isRunning()) {
      return true
    }

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

  private inner class KurobaBottomReplyPanelEpoxyController : EpoxyController() {
    var callback: EpoxyController.() -> Unit = {}
    var currentSpanCount = 1

    private val replyPanelSpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        try {
          val model = controller.adapter.getModelAtPosition(position)
          val itemCount = controller.adapter.itemCount

          if (itemCount <= 1 && model is EpoxyAttachNewFileButtonViewModel_) {
            return currentSpanCount
          }

          return 1
        } catch (ignored: Throwable) {
          return 1
        }
      }
    }

    override fun buildModels() {
      callback(this)
    }

    override fun getSpanSizeLookup(): GridLayoutManager.SpanSizeLookup {
      return replyPanelSpanSizeLookup
    }
  }

  interface KurobaBottomPanelCallbacks {
    suspend fun updateParentPanelHeight(newHeight: Int)
  }

  companion object {
    private const val TAG = "KurobaBottomReplyPanel"

    // TODO(KurobaEx): vvv remove me
    private val random = Random(System.currentTimeMillis())
    // TODO(KurobaEx): ^^^ remove me
  }

}