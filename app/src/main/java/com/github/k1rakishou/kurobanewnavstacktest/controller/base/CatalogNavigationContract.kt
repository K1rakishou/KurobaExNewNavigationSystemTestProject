package com.github.k1rakishou.kurobanewnavstacktest.controller.base

import com.github.k1rakishou.kurobanewnavstacktest.data.BoardDescriptor

interface CatalogNavigationContract {
    fun openBoard(boardDescriptor: BoardDescriptor)
}