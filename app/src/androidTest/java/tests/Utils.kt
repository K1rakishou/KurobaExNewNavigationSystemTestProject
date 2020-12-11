package tests

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.common.views.KView
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants


@Suppress("UNCHECKED_CAST")
inline fun <T : View> KView.doWithView(crossinline func: (T) -> Unit) {
  this.view.interaction.check { view, noViewFoundException ->
    if (noViewFoundException != null) {
      throw noViewFoundException
    }

    func((view as T))
  }
}

fun createTestModeIntent(): Intent {
  return Intent()
    .apply { putExtra(ViewStateConstants.MainActivity.testModeKey, true) }
}

fun <T : Activity> ActivityTestRule<T>.rotateScreenLandscape() {
  activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

fun <T : Activity> ActivityTestRule<T>.rotateScreenPortrait() {
  activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}