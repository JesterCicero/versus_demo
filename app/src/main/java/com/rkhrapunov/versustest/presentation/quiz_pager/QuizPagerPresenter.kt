package com.rkhrapunov.versustest.presentation.quiz_pager

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.core.interactors.PageIndicatorTextInteractor
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import org.koin.core.KoinComponent
import org.koin.core.inject

@FlowPreview
@ExperimentalCoroutinesApi
class QuizPagerPresenter : BasePresenter<IQuizPagerContract.IQuizPagerView>(),
    IQuizPagerContract.IQuizPagerPresenter, KoinComponent {

    private var mJob: Job? = null
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mPageIndicatorTextInteractor by inject<PageIndicatorTextInteractor>()

    override fun attachView(view: IQuizPagerContract.IQuizPagerView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        if (savedInstanceState == null) {
            mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mRenderUiChannelInteractor.getRenderUiChannel()
                    .asFlow()
                    .filter { it is RenderState.QuizListState }
                    .collect { mView?.renderQuizListState(it as RenderState.QuizListState) }
            }
        }
    }

    override fun savePageIndicatorText(pageIndicatorText: String) = mPageIndicatorTextInteractor.savePageIndicatorText(pageIndicatorText)

    override fun getPageIndicatorText() = mPageIndicatorTextInteractor.getPageIndicatorText()

    override fun saveCurrentPagePosition(currentPosition: Int) = mPageIndicatorTextInteractor.saveCurrentPagePosition(currentPosition)

    override fun getCurrentPagePosition() = mPageIndicatorTextInteractor.getCurrentPagePosition()

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }
}