package com.rkhrapunov.versustest.presentation.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
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
            .into(GlideImgViewTarget(imgView))
    }

    fun loadImage(context: Context, url: String, layout: ViewGroup) {
        Timber.d("url: $url")
        Glide.with(context)
            .asBitmap()
            .load("http://$url")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(GlideLayoutTarget(context, layout))
    }

    inner class GlideImgViewTarget(private val mImgView: ImageView) : CustomTarget<Bitmap>() {

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

    inner class GlideLayoutTarget(private val mContext: Context,
                                  private val mLayout: ViewGroup) : CustomTarget<Bitmap>() {

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                val bitmapDrawable = BitmapDrawable(mContext.resources, resource)
                mLayout.background = bitmapDrawable
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mLayout.setBackgroundResource(R.drawable.empty_drawable)
            }
        }
    }
}