package com.github.k1rakishou.kurobanewnavstacktest.widget.bottom_panel

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.ControllerType

class KurobaBottomHiddenPanel(
  context: Context,
) : ConstraintLayout(context, null, 0), ChildPanelContract {

  override suspend fun initializeView() {
    LayoutInflater.from(context).inflate(R.layout.kuroba_bottom_hidden_panel, this, true)
  }

  override fun getCurrentHeight(): Int {
    return 0
  }

  override fun getBackgroundColor(): Int {
    return context.resources.getColor(R.color.colorPrimaryDark)
  }

  override fun enableOrDisableControls(enable: Boolean) {
  }

  override fun restoreState(bottomPanelViewState: KurobaBottomPanelViewState) {
  }

  override fun updateCurrentControllerType(controllerType: ControllerType) {

  }

  override suspend fun updateHeight(parentHeight: Int) {

  }

  override fun handleBack(): Boolean {
    return false
  }

  override fun onDestroy() {

  }

}