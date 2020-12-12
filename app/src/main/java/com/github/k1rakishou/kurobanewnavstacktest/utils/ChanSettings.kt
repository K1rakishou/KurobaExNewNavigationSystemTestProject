package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.content.Context
import com.github.k1rakishou.kurobanewnavstacktest.utils.AndroidUtils.isLandscapeOrientation
import com.github.k1rakishou.kurobanewnavstacktest.utils.AndroidUtils.isTablet

object ChanSettings {
  val OVERHANG_SIZE = 20.dp

  fun collapsibleViewsAlwaysLocked(context: Context): Boolean {
    // TODO(KurobaEx): make this a separate setting when migrating this
    //  into the main project
    return isSplitMode(context) && isTablet()
  }

  fun isSplitMode(context: Context): Boolean {
    if (!AndroidUtils.isTestMode) {
      return false
    }

    return context.isLandscapeOrientation()
  }

}