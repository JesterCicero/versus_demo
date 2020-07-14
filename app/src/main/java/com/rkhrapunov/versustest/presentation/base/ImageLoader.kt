package com.rkhrapunov.versustest.presentation.base

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImageLoader : KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()

    fun loadImage(fragment: Fragment, url: String, imgView: ImageView) {

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .signature(ObjectKey(System.currentTimeMillis().toShort()))

        Glide.with(fragment)
            .asBitmap()
            .load("http://$url")
            .apply(requestOptions)
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