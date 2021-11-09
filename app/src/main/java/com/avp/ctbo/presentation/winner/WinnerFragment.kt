package com.avp.ctbo.presentation.winner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.ctbo.R
import com.avp.ctbo.databinding.FragmentWinnerBinding
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.Constants.SPACE_SYMBOL
import com.avp.ctbo.presentation.base.Constants.UNDERSCORE_SYMBOL
import com.avp.ctbo.presentation.base.ImageLoader
import com.avp.ctbo.presentation.base.capitalizeWords
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.android.ext.android.inject

@DelicateCoroutinesApi
class WinnerFragment : Fragment(), IWinnerContract.IWinnerView {

    private val mPresenter by inject<IWinnerContract.IWinnerPresenter>()
    private var mBinding: FragmentWinnerBinding? = null
    private val mImageLoader by inject<ImageLoader>()
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
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
        binding.winner.setBackgroundResource(R.drawable.selected_contestant_shape)
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
            mBinding?.let{
                it.statsButton.visibility = View.VISIBLE
                it.progressBar.visibility = View.GONE
            }
        }
    }

    override fun hideProgressBar() {
        mBinding?.let { it.progressBar.visibility = View.GONE }
    }

    private fun renderWinner(name: String, url: String) {
        mBinding?.let { binding ->
            binding.name = name.replace(UNDERSCORE_SYMBOL, SPACE_SYMBOL).capitalizeWords()
            binding.presenter = mPresenter
            activity?.let {
                mCoroutineLauncherHelper.launchImgLoading {
                    mImageLoader.loadImage(it, url, binding.winnerImgId)
                    mImageLoader.loadImage(it, mPresenter.getCurrentQuizBackgroundUrl(), binding.fragmentContainer)
                }
            }
        }
    }
}