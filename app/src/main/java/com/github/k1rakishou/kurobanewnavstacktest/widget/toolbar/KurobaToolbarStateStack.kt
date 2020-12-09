package com.github.k1rakishou.kurobanewnavstacktest.widget.toolbar

import java.util.*

class KurobaToolbarStateStack  {
  private val stateStack: Stack<ToolbarStateClass> = Stack<ToolbarStateClass>()

  fun getPrevToolbarStateClass(): ToolbarStateClass {
    if (stateStack.isEmpty()) {
      return ToolbarStateClass.Uninitialized
    }

    return stateStack.peek()
  }

  fun pushToolbarStateClass(toolbarStateClass: ToolbarStateClass) {
    if (stateStack.contains(toolbarStateClass)) {
      return
    }

    stateStack.push(toolbarStateClass)
  }

  fun popCurrentStateIfPossibleOrNull(): ToolbarStateClass? {
    if (stateStack.size <= 1) {
      // Toolbars must have at least one entry in their state stacks (the initial toolbar state) so
      // state stack is considered empty is we are trying to pop an element when stack has 1 or less
      // elements.
      return null
    }

    stateStack.pop()

    if (stateStack.isEmpty()) {
      return null
    }

    return stateStack.peek()
  }

  fun pop(): ToolbarStateClass? {
    if (stateStack.isEmpty()) {
      return null
    }

    stateStack.pop()

    if (stateStack.isEmpty()) {
      return null
    }

    return stateStack.peek()
  }

}