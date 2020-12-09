package com.github.k1rakishou.kurobanewnavstacktest

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.github.k1rakishou.kurobanewnavstacktest.utils.AndroidUtils
import timber.log.Timber


class MyApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    AndroidUtils.application = this
    Timber.plant(DebugTree())

    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
      Timber.e(exception, "Unhandled exception in thread ${thread.name}")
    }
  }

  private class DebugTree : Timber.Tree() {
    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      val tagString = if (tag != null) {
        " | $tag"
      } else {
        ""
      }

      when (priority) {
        Log.VERBOSE -> Log.d("${APP_TAG}${tagString}", message)
        Log.DEBUG -> Log.d("${APP_TAG}${tagString}", message)
        Log.INFO -> Log.d("${APP_TAG}${tagString}", message)
        Log.WARN -> Log.w("${APP_TAG}${tagString}", message)
        Log.ERROR -> Log.e("${APP_TAG}${tagString}", message, t)
        Log.ASSERT -> throw IllegalStateException("We don't use this here")
      }
    }

    companion object {
      private const val APP_TAG = "KurobaEx"
    }
  }
}