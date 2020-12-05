package com.rkhrapunov.versustest.presentation.base

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Bitmap
import android.graphics.Matrix
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
import kotlinx.coroutines.delay
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class ImageLoader : KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()

    companion object {
        private const val ROTATION_ANGLE = 90F
        private const val IMG_LOAD_RETRY_DELAY = 1000L
    }

    fun loadImage(context: Context, url: String, imgView: ImageView) {
        Timber.d("url: $url")
        Glide.with(context)
            .asBitmap()
            .load("http://$url")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(GlideImgViewTarget(context, imgView, url))
    }

    fun loadImage(context: Context, url: String, layout: ViewGroup) {
        Timber.d("url: $url")
        Glide.with(context)
            .asBitmap()
            .load("http://$url")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(GlideLayoutTarget(context, layout, url))
    }

    inner class GlideImgViewTarget(private val mContext: Context,
                                   imgView: ImageView,
                                   private val mUrl: String) : CustomTarget<Bitmap>() {

        private var mImgView: ImageView? by weak()

        init {
            mImgView = imgView
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            Timber.d("onLoadFailed()")
            mImgView?.let {
                mCoroutineLauncherHelper.launchImgLoading {
                    delay(IMG_LOAD_RETRY_DELAY)
                    Timber.d("onLoadFailed(): img url: $mUrl")
                    loadImage(mContext, mUrl, it)
                }
            }
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mImgView?.setImageBitmap(resource)
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mImgView?.setImageResource(R.drawable.empty_drawable)
            }
        }
    }

    inner class GlideLayoutTarget(private val mContext: Context,
                                  layout: ViewGroup,
                                  private val mUrl: String) : CustomTarget<Bitmap>() {

        private var mLayout: ViewGroup? by weak()

        init {
            mLayout = layout
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            Timber.d("onLoadFailed()")
            mLayout?.let {
                mCoroutineLauncherHelper.launchImgLoading {
                    delay(IMG_LOAD_RETRY_DELAY)
                    Timber.d("onLoadFailed(): img url: $mUrl")
                    loadImage(mContext, mUrl, it)
                }
            }
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                val bitmap = if (mContext.resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                    val matrix = Matrix()
                    matrix.postRotate(ROTATION_ANGLE)
                    Bitmap.createBitmap(resource, 0, 0, resource.width, resource.height, matrix, true)
                } else {
                    resource
                }
                mLayout?.background =  BitmapDrawable(mContext.resources, bitmap)
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mLayout?.setBackgroundResource(R.drawable.empty_drawable)
            }
        }
    }
}