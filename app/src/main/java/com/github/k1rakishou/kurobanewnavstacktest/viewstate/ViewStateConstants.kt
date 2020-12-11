package com.github.k1rakishou.kurobanewnavstacktest.viewstate

object ViewStateConstants {
  const val key = "view_state_constants"

  object MainActivity {
    const val key = "${ViewStateConstants.key}_main_activity"

    const val testModeKey = "${key}_test_mode"
  }

  object SplitNavController {
    const val key = "${ViewStateConstants.key}_split_nav_controller"
  }

  object ImageViewController {
    const val key = "${ViewStateConstants.key}_image_view_controller"

    const val postDescriptor = "${key}_post_descriptor"
  }

}