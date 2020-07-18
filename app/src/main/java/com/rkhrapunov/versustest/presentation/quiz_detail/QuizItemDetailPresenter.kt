package com.rkhrapunov.versustest.presentation.quiz_detail

import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.core.interactors.ChosenContestantInteractor
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
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
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class QuizItemDetailPresenter : BasePresenter<IQuizItemDetailContract.IQuizItemDetailView>(),
    IQuizItemDetailContract.IQuizItemDetailPresenter, KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mChosenContestantInteractor by inject<ChosenContestantInteractor>()
    private var mJob: Job? = null
    private var mCurrentState: IRenderState? = null
    private var mQuizItemStateUpdated = false

    override fun attachView(view: IQuizItemDetailContract.IQuizItemDetailView, viewLifecycle: Lifecycle) {
        super.attachView(view, viewLifecycle)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .filter { it is RenderState.QuizItemDetailState && it != mCurrentState }
                .collect { renderState ->
                    Timber.d("attachView(): renderState=$renderState")
                    if (renderState is RenderState.QuizItemDetailState) {
                        mQuizItemStateUpdated = true
                        mView?.renderQuitItemDetailState(renderState)
                        mCurrentState = renderState
                    }
                }
        }
    }

    override fun onFirstImgClickedIntent() = onItemClicked(true)

    override fun onSecondImgClickedIntent() = onItemClicked(false)

    private fun onItemClicked(chosenFirst: Boolean) {
        if (mQuizItemStateUpdated) {
            mQuizItemStateUpdated = false
            mCurrentState?.let { mChosenContestantInteractor.onChosenContestant(it, chosenFirst) }
        }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }
}