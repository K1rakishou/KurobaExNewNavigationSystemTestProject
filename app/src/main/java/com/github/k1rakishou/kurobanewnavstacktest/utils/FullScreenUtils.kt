package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

object FullScreenUtils {

  fun Window.setupFullscreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      attributes.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
  }

  fun Window.setupStatusAndNavBarColors() {
    var newSystemUiVisibility = decorView.systemUiVisibility

    if (AndroidUtils.isAndroidM()) {
      newSystemUiVisibility = newSystemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

    if (AndroidUtils.isAndroidO()) {
      newSystemUiVisibility =
        newSystemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
    }

    decorView.systemUiVisibility = newSystemUiVisibility
  }

  fun Window.isSystemUiHidden(): Boolean {
    return (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
  }

  fun Window.hideSystemUi() {
    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
      or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
      or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      or View.SYSTEM_UI_FLAG_FULLSCREEN)

    setupStatusAndNavBarColors()
  }

  fun Window.showSystemUi() {
    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

    setupStatusAndNavBarColors()
  }

  fun calculateDesiredRealBottomInset(
    view: View,
    bottomInset: Int
  ): Int {
    val hasKeyboard = isKeyboardShown(view, bottomInset)
    return if (hasKeyboard) {
      bottomInset
    } else {
      0
    }
  }

  fun isKeyboardShown(view: View, bottomInset: Int) =
    bottomInset / view.resources.displayMetrics.heightPixels.toDouble() > .25

}