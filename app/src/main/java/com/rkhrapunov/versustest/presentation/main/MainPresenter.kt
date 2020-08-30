package com.rkhrapunov.versustest.presentation.main

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.interactors.CancelQuizInteractor
import com.rkhrapunov.core.interactors.GetErrorMsgChannelInteractor
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class MainPresenter : BasePresenter<IMainContract.IMainView>(), IMainContract.IMainPresenter, KoinComponent {

    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mErrorMsgChannelInteractor by inject<GetErrorMsgChannelInteractor>()
    private val mCancelQuizInteractor by inject<CancelQuizInteractor>()
    private var mRenderUiJob: Job? = null
    private var mErrorMsgJob: Job? = null
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mCurrentState: IRenderState? = null

    override fun attachView(view: IMainContract.IMainView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        subscribeRenderUiChannel()
        subscribeErrorMsgChannel()
    }

    override fun cancelQuiz() = mCancelQuizInteractor.cancelQuiz()

    override fun onViewDestroyed() {
        mRenderUiJob?.cancel()
        mErrorMsgJob?.cancel()
        super.onViewDestroyed()
    }

    private fun subscribeRenderUiChannel() {
        mRenderUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .filter { it != mCurrentState }
                .collect {
                    Timber.d("$mCurrentState -> $it")
                    mView?.render(it)
                    mCurrentState = it
                }
        }
    }

    private fun subscribeErrorMsgChannel() {
        mErrorMsgJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mErrorMsgChannelInteractor.getErrorMsgChannel()
                .asFlow()
                .collect {
                    Timber.d("Error message: $it")
                    mView?.renderErrorState(it)
                }
        }
    }
}