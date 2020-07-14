package com.rkhrapunov.versustest.presentation.main

import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.framework.helpers.CustomDispatchers.singleCoroutineDispatcher
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class MainPresenter : BasePresenter<IMainContract.IMainView>(), IMainContract.IMainPresenter, KoinComponent {

    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private var mJob: Job? = null
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mCurrentState: IRenderState? = null

    override fun attachView(view: IMainContract.IMainView, viewLifecycle: Lifecycle) {
        super.attachView(view, viewLifecycle)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .flowOn(singleCoroutineDispatcher)
                .filter { it != mCurrentState }
                .collect {
                    Timber.d("state: $it")
                    mView?.render(it)
                    mCurrentState = it
            }
        }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }
}