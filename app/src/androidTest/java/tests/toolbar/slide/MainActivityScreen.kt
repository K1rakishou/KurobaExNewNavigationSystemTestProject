package tests.toolbar.slide

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object MainActivityScreen : KScreen<MainActivityScreen>() {
  override val layoutId: Int = R.layout.activity_main
  override val viewClass: Class<*> = MainActivity::class.java

  val slideToolbarKView = KView { withId(R.id.slide_toolbar_id) }

  val catalogSearchToolbarInput = KEditText {
    withId(R.id.search_toolbar_input)
    withTag(KurobaToolbarType.Catalog)
  }

  val threadSearchToolbarInput = KEditText {
    withId(R.id.search_toolbar_input)
    withTag(KurobaToolbarType.Thread)
  }

  val slidingPaneLayoutExKView = KView { withId(R.id.sliding_pane_layout) }
  val openSearchButton = KImageView { withId(R.id.open_search_button) }

  class CatalogThreadViewItem(parent: Matcher<View>) : KRecyclerItem<CatalogThreadViewItem>(parent) {
    val rootView: KView = KView(parent) { withId(R.id.catalog_thread_root) }
  }

  val catalogRecycler: KRecyclerView = KRecyclerView({
    withId(R.id.controller_catalog_epoxy_recycler_view)
  }, itemTypeBuilder = {
    itemType(::CatalogThreadViewItem)
  })
}