package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.content.Context
import android.content.res.Configuration
import com.github.k1rakishou.kurobanewnavstacktest.utils.AndroidUtils.isTablet

object ChanSettings {
  val OVERHANG_SIZE = 20.dp

  fun showLockCollapsableViews(context: Context): Boolean {
    // TODO(KurobaEx):
    return isSplitMode(context) && isTablet()
  }

  fun isSplitMode(context: Context): Boolean {
    if (!AndroidUtils.isTestMode) {
      return false
    }

    return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  }

}