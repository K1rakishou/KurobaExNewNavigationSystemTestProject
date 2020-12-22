package com.github.k1rakishou.kurobanewnavstacktest.viewstate

import android.os.Bundle
import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.data.ThreadDescriptor

object ViewStateConstants {
  const val key = "view_state_constants"

  object MainActivity {
    const val key = "${ViewStateConstants.key}_main_activity"

    const val testModeKey = "${key}_test_mode"
  }

  object SplitNavController {
    const val key = "${ViewStateConstants.key}_split_nav_controller"
  }

  object MainController {
    const val key = "${ViewStateConstants.key}_main_controller"

    const val openBoardDescriptorKey = "${key}_open_board_descriptor"
    const val openThreadDescriptorKey = "${key}_open_thread_descriptor"
  }

  object ImageViewController {
    const val key = "${ViewStateConstants.key}_image_view_controller"

    const val postDescriptor = "${key}_post_descriptor"
  }

}

fun Bundle.putBoardDescriptor(boardDescriptor: BoardDescriptor?) {
  putParcelable(ViewStateConstants.MainController.openBoardDescriptorKey, boardDescriptor)
}

fun Bundle.getBoardDescriptorOrNull(): BoardDescriptor? {
  return getParcelable(ViewStateConstants.MainController.openBoardDescriptorKey)
}

fun Bundle.putThreadDescriptor(threadDescriptor: ThreadDescriptor?) {
  putParcelable(ViewStateConstants.MainController.openThreadDescriptorKey, threadDescriptor)
}

fun Bundle.getThreadDescriptorOrNull(): ThreadDescriptor? {
  return getParcelable(ViewStateConstants.MainController.openThreadDescriptorKey)
}