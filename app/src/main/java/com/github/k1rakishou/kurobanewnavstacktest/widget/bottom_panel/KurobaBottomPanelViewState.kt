package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

data class KurobaBottomPanelViewState(
  var panelCurrentStateKind: KurobaBottomPanelStateKind = KurobaBottomPanelStateKind.Uninitialized,
  var panelInitialStateKind: KurobaBottomPanelStateKind = KurobaBottomPanelStateKind.Uninitialized,
  val bottomNavPanelState: KurobaBottomNavPanelViewState = KurobaBottomNavPanelViewState(),
  val bottomReplyPanelState: KurobaBottomReplyPanelViewState = KurobaBottomReplyPanelViewState()
) {

  fun currentStateAllowsFabUpdate(): Boolean {
    return panelCurrentStateKind == KurobaBottomPanelStateKind.BottomNavPanel
      || panelCurrentStateKind == KurobaBottomPanelStateKind.Uninitialized
      || panelCurrentStateKind == KurobaBottomPanelStateKind.Hidden
  }

}