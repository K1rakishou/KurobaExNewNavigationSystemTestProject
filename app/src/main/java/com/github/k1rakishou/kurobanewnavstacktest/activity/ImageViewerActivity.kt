package com.github.k1rakishou.kurobanewnavstacktest.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.k1rakishou.kurobanewnavstacktest.R
import com.github.k1rakishou.kurobanewnavstacktest.controller.ImageViewController
import com.github.k1rakishou.kurobanewnavstacktest.data.PostDescriptor
import com.github.k1rakishou.kurobanewnavstacktest.utils.FullScreenUtils.hideSystemUi
import com.github.k1rakishou.kurobanewnavstacktest.utils.FullScreenUtils.setupFullscreen
import com.github.k1rakishou.kurobanewnavstacktest.viewstate.ViewStateConstants
import com.github.k1rakishou.kurobanewnavstacktest.widget.TouchBlockingFrameLayout
import dev.chrisbanes.insetter.Insetter


class ImageViewerActivity : AppCompatActivity(), ActivityContract {
  private lateinit var router: Router
  private lateinit var rootContainer: TouchBlockingFrameLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_image_viewer)

    rootContainer = findViewById(R.id.root_container)
    router = Conductor.attachRouter(this, rootContainer, savedInstanceState)

    Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    window.setupFullscreen()
    window.hideSystemUi()

    if (!router.hasRootController()) {
      val args = intent.extras
      if (args == null) {
        finish()
        return
      }

      val postDescriptor =
        args.getParcelable<PostDescriptor>(ViewStateConstants.ImageViewController.postDescriptor)

      if (postDescriptor == null) {
        finish()
        return
      }

      val controller = ImageViewController.withPostDescriptor(postDescriptor)
      router.setRoot(RouterTransaction.with(controller))
    }
  }

  override fun activity(): AppCompatActivity {
    return this
  }
}