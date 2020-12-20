package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType
import com.github.k1rakishou.kurobanewnavstacktest.utils.setAlphaFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setEnabledFast
import com.github.k1rakishou.kurobanewnavstacktest.utils.setOnThrottlingClickListener

@SuppressLint("ViewConstructor")
class KurobaBottomNavPanel(
  context: Context,
  initialControllerType: ControllerType,
  private val viewModel: KurobaBottomPanelViewModel,
  private val callbacks: KurobaBottomPanelCallbacks
) : ConstraintLayout(context, null, 0), ChildPanelContract, View.OnClickListener {
  private var controllerType = initialControllerType

  private lateinit var searchItemHolder: LinearLayout
  private lateinit var bookmarksItemHolder: LinearLayout
  private lateinit var browseItemHolder: LinearLayout
  private lateinit var settingsItemHolder: LinearLayout
  private lateinit var itemsArray: Array<LinearLayout>

  private val viewState: KurobaBottomNavPanelViewState
    get() = viewModel.getBottomPanelState(controllerType).bottomNavPanelState

  override suspend fun initializeView() {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_nav_panel, this, true)

    searchItemHolder = findViewById<LinearLayout>(R.id.search_item_holder)
      .apply { tag = KurobaBottomNavPanelSelectedItem.Search }
    bookmarksItemHolder = findViewById<LinearLayout>(R.id.bookmarks_item_holder)
      .apply { tag = KurobaBottomNavPanelSelectedItem.Bookmarks }
    browseItemHolder = findViewById<LinearLayout>(R.id.browse_item_holder)
      .apply { tag = KurobaBottomNavPanelSelectedItem.Browse }
    settingsItemHolder = findViewById<LinearLayout>(R.id.settings_item_holder)
      .apply { tag = KurobaBottomNavPanelSelectedItem.Settings }

    itemsArray = arrayOf(
      searchItemHolder,
      bookmarksItemHolder,
      browseItemHolder,
      settingsItemHolder,
    )

    itemsArray.forEach { item -> item.setOnThrottlingClickListener(this) }
  }

  override suspend fun onPanelAttachedToParent() {

  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    select(KurobaBottomNavPanelSelectedItem.Browse)
  }

  override fun restoreState(bottomPanelViewState: KurobaBottomPanelViewState) {
    val oldState = bottomPanelViewState.bottomNavPanelState

    if (oldState.selectedItem != KurobaBottomNavPanelSelectedItem.Uninitialized) {
      selectInternal(oldState.selectedItem)
    }
  }

  override fun updateCurrentControllerType(controllerType: ControllerType) {
    this.controllerType = controllerType
  }

  override suspend fun updateHeight(parentHeight: Int) {

  }

  override fun getCurrentHeight(): Int {
    return context.resources.getDimension(R.dimen.bottom_nav_panel_height).toInt()
  }

  override fun getBackgroundColor(): Int {
    return context.resources.getColor(R.color.colorPrimaryDark)
  }

  override fun enableOrDisableControls(enable: Boolean) {
    searchItemHolder.setEnabledFast(enable)
    bookmarksItemHolder.setEnabledFast(enable)
    browseItemHolder.setEnabledFast(enable)
    settingsItemHolder.setEnabledFast(enable)
  }

  override fun onDestroy() {

  }

  override fun onClick(v: View?) {
    val selectedItem = when {
      v === searchItemHolder -> KurobaBottomNavPanelSelectedItem.Search
      v === bookmarksItemHolder -> KurobaBottomNavPanelSelectedItem.Bookmarks
      v === browseItemHolder -> KurobaBottomNavPanelSelectedItem.Browse
      v === settingsItemHolder -> KurobaBottomNavPanelSelectedItem.Settings
      else -> KurobaBottomNavPanelSelectedItem.Uninitialized
    }

    if (selectedItem == KurobaBottomNavPanelSelectedItem.Uninitialized) {
      return
    }

    if (!select(selectedItem)) {
      return
    }

    callbacks.onItemSelected(selectedItem)
  }

  override fun handleBack(): Boolean {
    return false
  }

  fun select(newSelectedItem: KurobaBottomNavPanelSelectedItem): Boolean {
    if (viewState.selectedItem == newSelectedItem) {
      return false
    }

    return selectInternal(newSelectedItem)
  }

  private fun selectInternal(newSelectedItem: KurobaBottomNavPanelSelectedItem): Boolean {
    require(newSelectedItem != KurobaBottomNavPanelSelectedItem.Uninitialized) {
      "Bad newSelectedItem: $newSelectedItem"
    }

    // TODO(KurobaEx): animations
    itemsArray.forEach { item ->
      if (item.tag == newSelectedItem) {
        item.setAlphaFast(1f)
      } else {
        item.setAlphaFast(.3f)
      }
    }

    viewState.selectedItem = newSelectedItem
    return true
  }

  interface KurobaBottomPanelCallbacks {
    fun onItemSelected(selectedItem: KurobaBottomNavPanelSelectedItem)
  }

}