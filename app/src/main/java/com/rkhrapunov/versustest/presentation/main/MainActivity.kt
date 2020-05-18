package com.rkhrapunov.versustest.presentation.main

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.databinding.ActivityMainBinding
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import org.koin.android.ext.android.inject
import timber.log.Timber


class MainActivity : AppCompatActivity(), IMainContract.IMainView {

    private val mPresenter by inject<IMainContract.IMainPresenter>()
    private var mBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = setContentView(this, R.layout.activity_main)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mPresenter.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mPresenter.onRestoreInstanceState(savedInstanceState)
    }

    override fun render(renderState: IRenderState) {
        if (renderState is RenderState.VersusState) {
            Timber.d("########## render VersusState")
            mBinding?.dataReady = true
            mBinding?.versusState = renderState
            mBinding?.round = renderState.round
            mBinding?.executePendingBindings()
        } else if (renderState is RenderState.WinnerState) {
            mBinding?.round = "The Winner"
            mBinding?.vsTextView?.visibility = View.GONE
            if (renderState.firstContestantWon) {
                mBinding?.secondImg?.visibility = View.GONE
                mBinding?.secondImgDescription?.visibility = View.GONE
                mBinding?.firstImageId?.setImageResource(renderState.winnerImgResId)
                mBinding?.firstImgDescription?.text = renderState.winnerImgDescription
            } else {
                mBinding?.firstImg?.visibility = View.GONE
                mBinding?.firstImgDescription?.visibility = View.GONE
                mBinding?.secondImageId?.setImageResource(renderState.winnerImgResId)
                mBinding?.secondImgDescription?.text = renderState.winnerImgDescription
            }
            mBinding?.executePendingBindings()
            Toast.makeText(this, "Click on Winner to start again", Toast.LENGTH_SHORT).show()
        }
    }

    fun onFirstImgClicked(view: View) {
//        val screenOrientation = resources.configuration.orientation
//        view.animate().translationX()
        mPresenter.onFirstImgClicked()
    }

    fun onSecondImgClicked(view: View) = mPresenter.onSecondImgClicked()

    override fun resetViews() {
        mBinding?.roundDescription?.text = EMPTY_STRING
        mBinding?.firstImageId?.setImageResource(R.drawable.empty_drawable)
        mBinding?.secondImageId?.setImageResource(R.drawable.empty_drawable)
        mBinding?.firstImgDescription?.text = EMPTY_STRING
        mBinding?.secondImgDescription?.text = EMPTY_STRING
        mBinding?.vsTextView?.visibility = View.VISIBLE
        mBinding?.firstImg?.visibility = View.VISIBLE
        mBinding?.firstImgDescription?.visibility = View.VISIBLE
        mBinding?.secondImg?.visibility = View.VISIBLE
        mBinding?.secondImgDescription?.visibility = View.VISIBLE
    }

//    private fun getTranslation(viewPropertyAnimator: ViewPropertyAnimator)
//            = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) viewPropertyAnimator.translationY()
}
