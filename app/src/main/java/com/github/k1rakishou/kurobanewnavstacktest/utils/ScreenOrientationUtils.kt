package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
import android.view.WindowManager


object ScreenOrientationUtils {

  fun lockScreenOrientation(activity: Activity) {
    val configuration = activity.resources.configuration
    val rotation = getRotation(activity)

    if (isLandscapeOrientationNaturalForDevice(configuration, rotation)) {
      when (rotation) {
        Surface.ROTATION_0 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Surface.ROTATION_90 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        Surface.ROTATION_180 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        Surface.ROTATION_270 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      }
    } else {
      when (rotation) {
        Surface.ROTATION_0 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Surface.ROTATION_90 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Surface.ROTATION_180 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        Surface.ROTATION_270 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
      }
    }
  }

  fun unlockScreenOrientation(activity: Activity) {
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  private fun isLandscapeOrientationNaturalForDevice(
    configuration: Configuration,
    rotation: Int
  ): Boolean {
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
    }

    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      return (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
    }

    return false
  }

  private fun getRotation(activity: Activity): Int {
    if (AndroidUtils.isAndroid11()) {
      return activity.display!!.rotation
    }

    val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return windowManager.defaultDisplay.rotation
  }

}