package com.rkhrapunov.versustest.presentation.quiz_detail

import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.core.interactors.ChosenContestantInteractor
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.framework.helpers.CustomDispatchers
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
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

    override fun attachView(view: IQuizItemDetailContract.IQuizItemDetailView, viewLifecycle: Lifecycle) {
        super.attachView(view, viewLifecycle)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .flowOn(CustomDispatchers.singleCoroutineDispatcher)
                .filter { it is RenderState.QuizItemDetailState && it != mCurrentState }
                .collect { renderState ->
                    Timber.d("attachView(): renderState=$renderState")
                    if (renderState is RenderState.QuizItemDetailState) {
                        mView?.renderQuitItemDetailState(renderState)
                        mCurrentState = renderState
                    }
                }
        }
    }

    override fun onFirstImgClickedIntent() {
        mCurrentState?.let { mChosenContestantInteractor.onChosenContestant(it, true) }
    }

    override fun onSecondImgClickedIntent() {
        mCurrentState?.let { mChosenContestantInteractor.onChosenContestant(it, false) }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }
}