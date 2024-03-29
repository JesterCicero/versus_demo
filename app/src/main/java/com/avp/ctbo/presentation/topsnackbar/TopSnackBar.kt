package com.avp.ctbo.presentation.topsnackbar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.avp.ctbo.R
import com.avp.ctbo.databinding.TopSnackbarBinding
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.pxFromDp
import com.avp.ctbo.presentation.base.weak
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

enum class TopSnackBarType {
    UNKNOWN,
    NO_CONNECTION
}

interface ITopSnackBarView {
    var detachWindowListener: () -> Unit
}

class TopSnackBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ITopSnackBarView {

    override var detachWindowListener: () -> Unit = {}
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        detachWindowListener()
    }
}

@DelicateCoroutinesApi
class TopSnackBar private constructor(activity: Activity?,
                                      private val mSnackBarView: View,
                                      private var mBinding: TopSnackbarBinding) : KoinComponent {
    private val mWindowManager: WindowManager? by weak(activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager)

    private var mTopSnackBarType = TopSnackBarType.UNKNOWN
    private val mContext by inject<Context>()
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mDismissListener: ITopBarNotification? = null
    private var mHideInterval: Int = DEFAULT_TIMEOUT_SEC
    private var mShown = false
    private var mTimerJob: Job? = null

    init {
        if (mSnackBarView is ITopSnackBarView) {
            mSnackBarView.detachWindowListener = { mTimerJob?.cancel() }
        }
        mBinding.topSnackBar = this
    }

    companion object {
        private const val SNACK_BAR_ELEVATION = 25f
        private const val ANIM_DURATION_MS = 250L
        const val DEFAULT_TIMEOUT_SEC = 15

        @SuppressLint("InflateParams")
        fun createTopSnackBar(activity: Activity): TopSnackBar {
            val binding = TopSnackbarBinding.inflate(activity.layoutInflater)
            return TopSnackBar(activity, binding.root, binding)
        }
    }

    fun setType(type: TopSnackBarType): TopSnackBar {
        mTopSnackBarType = type
        return this
    }

    fun setIcon(resId: Int): TopSnackBar {
        mBinding.snackBarIcon.setImageResource(resId)
        return this
    }

    fun setText(text: String): TopSnackBar {
        mBinding.snackBarText.text = text
        return this
    }

    fun onButtonClicked() {
        mTimerJob?.cancel()
        mDismissListener?.let {
            it.action()
        }
    }

    fun setHideTimeout(intervalMilliSeconds: Int): TopSnackBar {
        mHideInterval = intervalMilliSeconds
        return this
    }

    fun setOnDismissListener(listener: ITopBarNotification): TopSnackBar {
        mDismissListener = listener
        return this
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun show() {
        if (mShown) {
            Timber.d("show: already")
            return
        }
        mShown = true
        addSnackBar()
        mSnackBarView.animate()
            .translationY(0f)
            .setDuration(ANIM_DURATION_MS)
            .start()
        mSnackBarView.visibility = VISIBLE
        if (mHideInterval > 0) {
            Timber.d("Start hiding timer, interval = $mHideInterval")
            mTimerJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                delay(mHideInterval.toLong())
                Timber.d("Hide snack bar")
                mDismissListener?.let { it.action() }
                hide()
            }
        }
    }

    fun removeTopSnackBar() {
        if (mShown) {
            mShown = false
            Timber.d("removeTopSnackBar(): $mTopSnackBarType")
            mSnackBarView.visibility = View.GONE
            mWindowManager?.removeView(mSnackBarView)
        }
    }

    fun hide() {
        if (!mShown) {
            Timber.d("hide: already")
            return
        }
        try {
            mSnackBarView.animate()
                .translationY(-pxFromDp(mContext, mContext.resources.getInteger(R.integer.top_snack_bar_height).toFloat()))
                .setDuration(ANIM_DURATION_MS)
                .withEndAction {
                    removeTopSnackBar()
                }
                .start()
        } catch (e: Exception) {
            Timber.e(e, "Hiding snack bar exception")
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun addSnackBar() {
        mSnackBarView.elevation = SNACK_BAR_ELEVATION
        mSnackBarView.translationY = -pxFromDp(mContext, mContext.resources.getInteger(R.integer.top_snack_bar_height).toFloat())
        val params = WindowManager.LayoutParams()
        params.gravity = Gravity.CENTER or Gravity.TOP
        params.height = pxFromDp(mContext, mContext.resources.getInteger(R.integer.top_snack_bar_height).toFloat()).toInt()
        params.width = pxFromDp(mContext, if (mContext.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            mContext.resources.getInteger(R.integer.top_snack_bar_width).toFloat()
        } else {
            mContext.resources.getInteger(R.integer.top_snack_bar_land_width).toFloat()
        }
        ).toInt()
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        mWindowManager?.addView(mSnackBarView, params)
    }
}