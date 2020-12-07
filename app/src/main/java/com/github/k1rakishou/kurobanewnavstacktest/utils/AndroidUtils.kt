package com.github.k1rakishou.kurobanewnavstacktest.utils

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.annotation.BoolRes
import com.github.k1rakishou.kurobanewnavstacktest.R

object AndroidUtils {
    lateinit var application: Application
    const val isTestMode = true

    fun getResources(): Resources = application.resources
    fun getBoolRes(@BoolRes id: Int): Boolean = getResources().getBoolean(id)
    fun isTablet(): Boolean = getBoolRes(R.bool.isTablet)

    fun showLockCollapsableViews(context: Context): Boolean {
        // TODO(KurobaEx):
        // isSplitMode(context) && isTablet()
        return false
    }

    fun isSplitMode(context: Context): Boolean {
        if (!isTestMode) {
            return false
        }

        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun isAndroid11(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    fun isAndroid10(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isAndroidO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isAndroidL_MR1(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
    }

    fun isAndroidP(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun isAndroidM(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}