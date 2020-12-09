package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.search

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.doIgnoringTextWatcher
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnThrottlingClickListener
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import timber.log.Timber

@SuppressLint("ViewConstructor")
class KurobaSearchToolbar(
  context: Context,
  private val kurobaToolbarViewModel: KurobaToolbarViewModel,
  private val kurobaToolbarCallbacks: KurobaToolbarCallbacks
) : ConstraintLayout(context), KurobaToolbarDelegateContract<KurobaSearchToolbarState> {
  private val closeSearchButton: AppCompatImageView
  private val searchInput: TextInputEditText
  private val clearSearchInputButton: AppCompatImageView
  private val searchResults: MaterialTextView
  private val textWatcher: TextWatcher

  override val toolbarStateClass: ToolbarStateClass
    get() = ToolbarStateClass.Search

  init {
    inflate(context, R.layout.kuroba_search_toolbar, this).apply {
      closeSearchButton = findViewById(R.id.close_search_button)
      searchInput = findViewById(R.id.search_input)
      clearSearchInputButton = findViewById(R.id.clear_search_input_button)
      searchResults = findViewById(R.id.search_results)

      textWatcher = searchInput.doOnTextChanged { text, start, before, count ->
        kurobaToolbarViewModel.getToolbarState<KurobaSearchToolbarState>(toolbarStateClass)
          .updateQuery(text?.toString())

        kurobaToolbarViewModel.fireAction(ToolbarAction.Search.QueryUpdated(text?.toString()))
      }

      closeSearchButton.setOnThrottlingClickListener {
        kurobaToolbarCallbacks.popCurrentToolbarStateClass()
      }
      clearSearchInputButton.setOnThrottlingClickListener {
        searchInput.text = null
      }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun applyStateToUi(toolbarState: KurobaSearchToolbarState) {
    Timber.tag(TAG).d("applyStateToUi() toolbarState=$toolbarState")

    toolbarState.query?.let { query ->
      if (searchInput.text?.toString() == query) {
        return@let
      }

      searchInput.doIgnoringTextWatcher(textWatcher) {
        setText(query)
      }
    }

    toolbarState.foundItems?.let { foundItems ->
      searchResults.text = "${foundItems.currentItemIndex} / ${foundItems.items.size}"
    }
  }

  companion object {
    private const val TAG = "KurobaSearchToolbar"
  }
}