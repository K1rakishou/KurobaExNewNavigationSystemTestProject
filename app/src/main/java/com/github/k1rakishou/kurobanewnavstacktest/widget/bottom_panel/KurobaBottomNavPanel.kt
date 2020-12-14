package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import androidx.customview.view.AbsSavedState
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.utils.setAlphaFast
import com.github.k1rakishou.kurobanewnavstacktest.widget.TouchBlockingFrameLayout

@SuppressLint("ViewConstructor")
class KurobaBottomNavPanel (
  context: Context,
  private val callbacks: KurobaBottomPanelCallbacks
) : TouchBlockingFrameLayout(context, null, 0), View.OnClickListener {
  private var selectedItem = SelectedItem.Uninitialized

  private val searchItemHolder: LinearLayout
  private val bookmarksItemHolder: LinearLayout
  private val browseItemHolder: LinearLayout
  private val settingsItemHolder: LinearLayout
  private val itemsArray: Array<LinearLayout>

  init {
    inflate(context, R.layout.kuroba_bottom_nav_panel, this)

    searchItemHolder = findViewById<LinearLayout>(R.id.search_item_holder)
      .apply { tag = SelectedItem.Search }
    bookmarksItemHolder = findViewById<LinearLayout>(R.id.bookmarks_item_holder)
      .apply { tag = SelectedItem.Bookmarks }
    browseItemHolder = findViewById<LinearLayout>(R.id.browse_item_holder)
      .apply { tag = SelectedItem.Browse }
    settingsItemHolder = findViewById<LinearLayout>(R.id.settings_item_holder)
      .apply { tag = SelectedItem.Settings }

    itemsArray = arrayOf(
      searchItemHolder,
      bookmarksItemHolder,
      browseItemHolder,
      settingsItemHolder,
    )

    itemsArray.forEach { item -> item.setOnClickListener(this) }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    select(SelectedItem.Browse)
  }

  override fun onClick(v: View?) {
    val selectedItem = when {
      v === searchItemHolder -> SelectedItem.Search
      v === bookmarksItemHolder -> SelectedItem.Bookmarks
      v === browseItemHolder -> SelectedItem.Browse
      v === settingsItemHolder -> SelectedItem.Settings
      else -> SelectedItem.Uninitialized
    }

    if (selectedItem == SelectedItem.Uninitialized) {
      return
    }

    if (!select(selectedItem)) {
      return
    }

    callbacks.onItemSelected(selectedItem)
  }

  fun select(newSelectedItem: SelectedItem): Boolean {
    require(newSelectedItem != SelectedItem.Uninitialized) { "Bad newSelectedItem: $newSelectedItem" }

    if (selectedItem == newSelectedItem) {
      return false
    }

    // TODO(KurobaEx): animations
    itemsArray.forEach { item ->
      if (item.tag == newSelectedItem) {
        item.setAlphaFast(1f)
      } else {
        item.setAlphaFast(.3f)
      }
    }

    return true
  }

  override fun onSaveInstanceState(): Parcelable? {
    val superState = super.onSaveInstanceState()
      ?: return null
    val savedState = SavedState(superState)
    savedState.selectedItem = selectedItem

    return savedState
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state !is SavedState) {
      super.onRestoreInstanceState(state)
      return
    }

    super.onRestoreInstanceState(state.superState)

    if (state.selectedItem == null || state.selectedItem == SelectedItem.Uninitialized) {
      return
    }

    select(state.selectedItem!!)
  }

  fun getCurrentHeight(): Int {
    return context.resources.getDimension(R.dimen.bottom_nav_panel_height).toInt()
  }

  enum class SelectedItem(val value: Int) {
    Uninitialized(-1),
    Search(0),
    Bookmarks(1),
    Browse(2),
    Settings(3);

    companion object {
      fun fromInt(value: Int): SelectedItem {
        return values().firstOrNull { selectedItem -> selectedItem.value == value }
          ?: Uninitialized
      }
    }
  }

  class SavedState : AbsSavedState {
    var selectedItem: SelectedItem? = null

    constructor(superState: Parcelable) : super(superState)

    constructor(parcel: Parcel, loader: ClassLoader?) : super(parcel, loader) {
      selectedItem = SelectedItem.fromInt(parcel.readInt())
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
      super.writeToParcel(dest, flags)

      if (dest == null) {
        return
      }

      selectedItem?.let { item -> dest.writeInt(item.value) }
    }

  }

  interface KurobaBottomPanelCallbacks {
    fun onItemSelected(selectedItem: SelectedItem)
  }

}