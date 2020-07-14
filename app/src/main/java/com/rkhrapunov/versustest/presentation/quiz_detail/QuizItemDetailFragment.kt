package com.rkhrapunov.versustest.presentation.quiz_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.databinding.FragmentQuizItemDetailBinding
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class QuizItemDetailFragment : Fragment(), IQuizItemDetailContract.IQuizItemDetailView {

    private val mPresenter by inject<IQuizItemDetailContract.IQuizItemDetailPresenter>()
    private var mBinding: FragmentQuizItemDetailBinding? = null
    private val mImageLoader by inject<ImageLoader>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentQuizItemDetailBinding.inflate(inflater, container, false)
        mBinding = binding
        mBinding?.presenter = mPresenter
        return binding.root
    }

    override suspend fun renderQuitItemDetailState(renderState: RenderState.QuizItemDetailState) {
        Timber.d("round: ${renderState.round}")
        mBinding?.let {
            it.quizItemDetailState = renderState
            withContext(Dispatchers.IO) {
                mImageLoader.loadImage(this@QuizItemDetailFragment, renderState.firstContestant.url, it.firstImageId)
                mImageLoader.loadImage(this@QuizItemDetailFragment, renderState.secondContestant.url, it.secondImageId)
            }
        }
    }
}