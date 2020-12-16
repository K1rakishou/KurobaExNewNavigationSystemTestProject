package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import androidx.annotation.VisibleForTesting
import java.util.*

class KurobaToolbarStateStack(
  private val kurobaToolbarType: KurobaToolbarType
) {
  private val stateStack: Stack<ToolbarStateClass> = Stack<ToolbarStateClass>()

  @VisibleForTesting
  fun getStack() = stateStack

  fun getPrevToolbarStateClass(): ToolbarStateClass {
    if (stateStack.isEmpty()) {
      return ToolbarStateClass.Uninitialized
    }

    return stateStack.peek()
  }

  fun pushToolbarStateClass(toolbarStateClass: ToolbarStateClass): Boolean {
    if (stateStack.contains(toolbarStateClass)) {
      return false
    }

    stateStack.push(toolbarStateClass)
    return true
  }

  fun isTop(toolbarStateClass: ToolbarStateClass): Boolean {
    ensureStackCorrect()

    return stateStack.peek() == toolbarStateClass
  }

  fun popIfStateOnTop(targetStateClass: ToolbarStateClass): ToolbarStateClass? {
    ensureStackCorrect()

    if (stateStack.peek() != targetStateClass) {
      return null
    }

    return popCurrentStateIfPossibleOrNull()
  }

  fun popCurrentStateIfPossibleOrNull(): ToolbarStateClass? {
    ensureStackCorrect()

    if (stateStack.size <= 2) {
      // Toolbars must have at least two entries in their state stacks (Uninitialized toolbar state
      // and initial toolbar state) so state stack is considered empty is we are trying to pop an
      // element when stack has 2 or less elements.
      return null
    }

    if (stateStack.peek() == ToolbarStateClass.Uninitialized) {
      return null
    }

    stateStack.pop()

    if (stateStack.isEmpty()) {
      return null
    }

    val top = stateStack.peek()
    if (top == ToolbarStateClass.Uninitialized) {
      return null
    }

    return stateStack.peek()
  }

  fun clearState(): Boolean {
    ensureStackCorrect()

    if (stateStack.isEmpty() || stateStack.peek() == ToolbarStateClass.Uninitialized) {
      return false
    }

    stateStack.clear()
    stateStack.push(ToolbarStateClass.Uninitialized)

    return true
  }

  private fun ensureStackCorrect() {
    check(stateStack.isNotEmpty()) { "Stack must not be empty" }

    check(stateStack[0] == ToolbarStateClass.Uninitialized) {
      "0th state class must be ToolbarStateClass.Uninitialized"
    }

    if (stateStack.size >= 2) {
      when (kurobaToolbarType) {
        KurobaToolbarType.Catalog -> {
          check(stateStack[1] == ToolbarStateClass.Catalog) {
            "Expected: ToolbarStateClass.Catalog actual: ${stateStack[1]}"
          }
        }
        KurobaToolbarType.Thread -> {
          check(stateStack[1] == ToolbarStateClass.Thread) {
            "Expected: ToolbarStateClass.Thread actual: ${stateStack[1]}"
          }
        }
      }
    }
  }

}