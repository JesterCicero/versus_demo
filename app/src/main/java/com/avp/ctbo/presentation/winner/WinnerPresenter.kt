package com.avp.ctbo.presentation.winner

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.GetErrorMsgChannelInteractor
import com.avp.core.interactors.ResetInteractor
import com.avp.core.interactors.GetStatsInteractor
import com.avp.core.interactors.GetCurrentQuizInteractor
import com.avp.ctbo.framework.ContestantsCache
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.BasePresenter
import com.avp.ctbo.presentation.base.Constants
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@DelicateCoroutinesApi
class WinnerPresenter : BasePresenter<IWinnerContract.IWinnerView>(),
    IWinnerContract.IWinnerPresenter, KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mErrorMsgChannelInteractor by inject<GetErrorMsgChannelInteractor>()
    // Leave this interactor for possible future use
    @Suppress("unused")
    private val mResetInteractor by inject<ResetInteractor>()
    private val mGetStatsInteractor by inject<GetStatsInteractor>()
    private val mGetCurrentQuizInteractor by inject<GetCurrentQuizInteractor>()
    private val mContestantsCache by inject<ContestantsCache>()
    private var mJob: Job? = null
    private var mErrorMsgJob: Job? = null
    private var mCurrentState: IRenderState? = null

    override fun attachView(view: IWinnerContract.IWinnerView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        subscribeErrorMsgChannel()
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .filter {
                    (it is RenderState.WinnerState || it is RenderState.WinnerFinalState) && it != mCurrentState
                }
                .collect { renderState ->
                    Timber.d("attachView(): renderState=$renderState")
                    mView?.renderWinnerState(renderState)
                    mCurrentState = renderState
                }
        }
    }

    override fun onWinnerClickedIntent() {
        // Leave this code for possible future use
//        if (mCurrentState is RenderState.WinnerFinalState) {
//            mResetInteractor.resetContest()
//        }
    }

    override fun onViewStatsClickedIntent() = mGetStatsInteractor.getStats()

    override fun getCurrentQuizBackgroundUrl(): String {
        val filteredQuizzes = mContestantsCache.quizzesInfoCache?.filter {
            it.title == mGetCurrentQuizInteractor.getCurrentQuiz()
        }
        return filteredQuizzes?.let {
            if (it.isNotEmpty()) it[0].backgroundUrl else Constants.EMPTY_STRING
        } ?: Constants.EMPTY_STRING
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        mErrorMsgJob?.cancel()
        super.onViewDestroyed()
    }

    private fun subscribeErrorMsgChannel() {
        mErrorMsgJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mErrorMsgChannelInteractor.getErrorMsgChannel()
                .collect {
                    Timber.d("Error message: $it")
                    mView?.hideProgressBar()
                }
        }
    }
}