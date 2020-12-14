package com.github.k1rakishou.kurobanewnavstacktest.controller.base

interface CollapsableView {
  fun isLaidOut(): Boolean
  fun height(): Float
  fun translationY(): Float
  fun translationY(newTranslationY: Float)
}