package com.rkhrapunov.versustest.presentation.winner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.databinding.FragmentWinnerBinding
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class WinnerFragment : Fragment(), IWinnerContract.IWinnerView {

    private val mPresenter by inject<IWinnerContract.IWinnerPresenter>()
    private var mBinding: FragmentWinnerBinding? = null
    private val mImageLoader by inject<ImageLoader>()
    private var mShouldShowWinnerFinalState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mShouldShowWinnerFinalState = true
        }
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentWinnerBinding.inflate(inflater, container, false)
        mBinding = binding
        return binding.root
    }

    override suspend fun renderWinnerState(renderState: IRenderState) {
        if (renderState is RenderState.WinnerState) {
            renderWinner(renderState.winner.name, renderState.winner.url)
        } else if (renderState is RenderState.WinnerFinalState) {
            if (mShouldShowWinnerFinalState) {
                renderWinner(renderState.winner.name, renderState.winner.url)
                mShouldShowWinnerFinalState = false
            }
            mBinding?.let{ it.statsButton.visibility = View.VISIBLE }
        }
    }

    private suspend fun renderWinner(name: String, url: String) {
        mBinding?.let {
            it.name = name
            it.presenter = mPresenter
            withContext(Dispatchers.IO) { mImageLoader.loadImage(this@WinnerFragment, url, it.winnerImgId) }
        }
    }
}