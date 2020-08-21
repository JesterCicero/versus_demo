package com.rkhrapunov.versustest.presentation.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class ImageLoader : KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()

    fun loadImage(context: Context, url: String, imgView: ImageView) {
        Timber.d("url: $url")
        Glide.with(context)
            .asBitmap()
            .load("http://$url")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(GlideCustomTarget(imgView))
    }

    inner class GlideCustomTarget(private val mImgView: ImageView) : CustomTarget<Bitmap>() {

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mImgView.setImageBitmap(resource)
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mImgView.setImageResource(R.drawable.empty_drawable)
            }
        }
    }
}