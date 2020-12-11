package tests.toolbar.slide

import androidx.test.rule.ActivityTestRule
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.activity.MainActivity
import com.github.k1rakishou.kurobanewnavstacktest.core.test.TestHelpers
import com.github.k1rakishou.kurobanewnavstacktest.widget.SlidingPaneLayoutEx
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.KurobaToolbarType
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.SlideToolbar
import com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar.ToolbarStateClass
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import tests.createTestModeIntent
import tests.doWithView

class SlideToolbarTests : TestCase() {

  @get:Rule
  val activityTestRule = ActivityTestRule(MainActivity::class.java, true, false)

  val testHelpers: TestHelpers
    get() = activityTestRule.activity.testHelpers

  @Test
  fun test_simple_switch_into_search_mode_and_back() {
    run {
      step("Wait until everything is loaded") {
        activityTestRule.launchActivity(createTestModeIntent())

        testLogger.d("Start waiting for catalogLoadedLatch...")
        testHelpers.catalogLoadedLatch.await()
        testLogger.d("Start waiting for catalogLoadedLatch done!")
      }
      step("Switch into search mode and type text") {
        MainActivityScreen {
          openSearchButton.click()
          catalogSearchToolbarInput.typeText("Catalog toolbar")

          slideToolbarKView.doWithView<SlideToolbar> { slideToolbar ->
            val viewModel = slideToolbar.getCatalogToolbar().getViewModel()
            val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Catalog).getStack()

            check(stateStack.size == 3) { "Unexpected stateStack: $stateStack, expected 3" }

            arrayOf(
              ToolbarStateClass.Uninitialized,
              ToolbarStateClass.Catalog,
              ToolbarStateClass.Search,
            ).forEachIndexed { index, toolbarStateClass ->
              check(stateStack[index] == toolbarStateClass) {
                "Unexpected state class at $index: ${stateStack[index]}"
              }
            }
          }
        }
      }
      step("Press back to close toolbar search") {
        MainActivityScreen {
          // Hide the keyboard
          device.exploit.pressBack(failTestIfAppUnderTestClosed = true)
          // Close the search
          device.exploit.pressBack(failTestIfAppUnderTestClosed = true)

          slideToolbarKView.doWithView<SlideToolbar> { slideToolbar ->
            val viewModel = slideToolbar.getCatalogToolbar().getViewModel()
            val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Catalog).getStack()

            check(stateStack.size == 2) { "Unexpected stateStack: $stateStack, expected 2" }

            arrayOf(
              ToolbarStateClass.Uninitialized,
              ToolbarStateClass.Catalog
            ).forEachIndexed { index, toolbarStateClass ->
              check(stateStack[index] == toolbarStateClass) {
                "Unexpected state class at $index: ${stateStack[index]}"
              }
            }
          }
        }
      }
      step("Open the search again and check that it's empty") {
        MainActivityScreen {
          openSearchButton.click()
          catalogSearchToolbarInput.hasHint(R.string.toolbar_search_input_hint)
        }
      }
    }
  }

  @Test
  fun test_switching_between_catalog_and_thread_toolbars_in_search_mode() {
    run {
      step("Wait until everything is loaded") {
        activityTestRule.launchActivity(createTestModeIntent())

        testLogger.d("Start waiting for catalogLoadedLatch...")
        testHelpers.catalogLoadedLatch.await()
        testLogger.d("Start waiting for catalogLoadedLatch done!")
      }
      step("Open catalog search and enter query") {
        MainActivityScreen {
          openSearchButton.click()
          catalogSearchToolbarInput.typeText("Catalog toolbar")

          slideToolbarKView.doWithView<SlideToolbar> { slideToolbar ->
            val viewModel = slideToolbar.getCatalogToolbar().getViewModel()
            val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Catalog).getStack()

            check(stateStack.size == 3) { "Unexpected stateStack: $stateStack, expected 3" }

            arrayOf(
              ToolbarStateClass.Uninitialized,
              ToolbarStateClass.Catalog,
              ToolbarStateClass.Search,
            ).forEachIndexed { index, toolbarStateClass ->
              check(stateStack[index] == toolbarStateClass) {
                "Unexpected state class at $index: ${stateStack[index]}"
              }
            }
          }
        }
      }
      step("Switch to thread toolbar and enter query") {
        MainActivityScreen {
          catalogRecycler {
            firstChild<MainActivityScreen.CatalogThreadViewItem> {
              isVisible()
              rootView.click()
            }
          }

          openSearchButton.click()
          threadSearchToolbarInput.typeText("Thread toolbar")

          slideToolbarKView.doWithView<SlideToolbar> { slideToolbar ->
            val viewModel = slideToolbar.getThreadToolbar().getViewModel()
            val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Thread).getStack()

            check(stateStack.size == 3) { "Unexpected stateStack: $stateStack, expected 3" }

            arrayOf(
              ToolbarStateClass.Uninitialized,
              ToolbarStateClass.Thread,
              ToolbarStateClass.Search,
            ).forEachIndexed { index, toolbarStateClass ->
              check(stateStack[index] == toolbarStateClass) {
                "Unexpected state class at $index: ${stateStack[index]}"
              }
            }
          }
        }
      }
      step("Switch to catalog toolbar and check the entered query is still there") {
        MainActivityScreen {
          slidingPaneLayoutExKView.doWithView<SlidingPaneLayoutEx> { slidingPaneLayoutEx ->
            slidingPaneLayoutEx.openSlidingPaneLayout()
          }

          catalogSearchToolbarInput.hasText("Catalog toolbar")
        }
      }
      step("Switch to thread toolbar and check the entered query is still there") {
        MainActivityScreen {
          slidingPaneLayoutExKView.doWithView<SlidingPaneLayoutEx> { slidingPaneLayoutEx ->
            slidingPaneLayoutEx.closeSlidingPaneLayout()
          }

          threadSearchToolbarInput.hasText("Thread toolbar")
        }
      }
      // TODO(KurobaEx): uncomment test once the controller bug is fixed
//      step("Rotate screen and check the state") {
//        activityTestRule.rotateScreenLandscape()
//
//        MainActivityScreen {
//          slideToolbarKView.doWithView<SlideToolbar> { slideToolbar ->
//            run {
//              val viewModel = slideToolbar.getCatalogToolbar().getViewModel()
//              val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Catalog).getStack()
//
//              check(stateStack.size == 3) { "Unexpected stateStack: $stateStack, expected 3" }
//
//              arrayOf(
//                ToolbarStateClass.Uninitialized,
//                ToolbarStateClass.Catalog,
//                ToolbarStateClass.Search,
//              ).forEachIndexed { index, toolbarStateClass ->
//                check(stateStack[index] == toolbarStateClass) {
//                  "Unexpected state class at $index: ${stateStack[index]}"
//                }
//              }
//            }
//
//            run {
//              val viewModel = slideToolbar.getThreadToolbar().getViewModel()
//              val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Thread).getStack()
//
//              check(stateStack.size == 3) { "Unexpected stateStack: $stateStack, expected 3" }
//
//              arrayOf(
//                ToolbarStateClass.Uninitialized,
//                ToolbarStateClass.Thread,
//                ToolbarStateClass.Search,
//              ).forEachIndexed { index, toolbarStateClass ->
//                check(stateStack[index] == toolbarStateClass) {
//                  "Unexpected state class at $index: ${stateStack[index]}"
//                }
//              }
//            }
//          }
//        }
//
//        activityTestRule.rotateScreenPortrait()
//      }
      step("Press back until everything is closed and check that everything is closed") {
        // Close the keyboard
        device.exploit.pressBack(failTestIfAppUnderTestClosed = true)
        // Close the thread search toolbar
        device.exploit.pressBack(failTestIfAppUnderTestClosed = true)
        // Switch to catalog controller
        device.exploit.pressBack(failTestIfAppUnderTestClosed = true)
        // Close catalog's toolbar
        device.exploit.pressBack(failTestIfAppUnderTestClosed = true)

        MainActivityScreen {
          slideToolbarKView.doWithView<SlideToolbar> { slideToolbar ->
            kotlin.run {
              val viewModel = slideToolbar.getCatalogToolbar().getViewModel()
              val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Catalog).getStack()
              check(stateStack.size == 2) { "Unexpected stateStack (Catalog): $stateStack, expected 2" }

              arrayOf(
                ToolbarStateClass.Uninitialized,
                ToolbarStateClass.Catalog,
              ).forEachIndexed { index, toolbarStateClass ->
                check(stateStack[index] == toolbarStateClass) {
                  "Unexpected state class at $index: ${stateStack[index]}"
                }
              }
            }

            kotlin.run {
              val viewModel = slideToolbar.getThreadToolbar().getViewModel()
              val stateStack = viewModel.getToolbarStateStack(KurobaToolbarType.Thread).getStack()
              check(stateStack.size == 2) { "Unexpected stateStack (Thread): $stateStack, expected 2" }

              arrayOf(
                ToolbarStateClass.Uninitialized,
                ToolbarStateClass.Thread,
              ).forEachIndexed { index, toolbarStateClass ->
                check(stateStack[index] == toolbarStateClass) {
                  "Unexpected state class at $index: ${stateStack[index]}"
                }
              }
            }
          }
        }
      }
    }
  }

  private fun SlidingPaneLayoutEx.openSlidingPaneLayout() {
    check(!isOpen) { "Must be closed" }
    open()
    Thread.sleep(300L)
  }

  private fun SlidingPaneLayoutEx.closeSlidingPaneLayout() {
    check(isOpen) { "Must be opened" }
    close()
    Thread.sleep(300L)
  }
}