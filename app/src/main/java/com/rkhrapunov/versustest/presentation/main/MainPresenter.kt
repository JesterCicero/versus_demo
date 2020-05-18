package com.rkhrapunov.versustest.presentation.main

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.core.interactors.ChosenContestantInteractor
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.core.interactors.ResetInteractor
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.framework.helpers.CustomDispatchers.singleCoroutineDispatcher
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class MainPresenter : BasePresenter<IMainContract.IMainView>(), IMainContract.IMainPresenter, KoinComponent {

    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mChosenContestantInteractor by inject<ChosenContestantInteractor>()
    private val mResetInteractor by inject<ResetInteractor>()
    private var mJob: Job? = null
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mCurrentState: IRenderState? = null

    companion object {
        private const val FIRST_IMAGE_DESCRIPTION = "first_image_description"
        private const val SECOND_IMAGE_DESCRIPTION = "second_image_description"
        private const val FIRST_IMAGE_RES_ID = "first_image_res_id"
        private const val SECOND_IMAGE_RES_ID = "second_image_res_id"
    }

    override fun attachView(view: IMainContract.IMainView, viewLifecycle: Lifecycle) {
        Timber.d("######### attachView")
        super.attachView(view, viewLifecycle)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel().asFlow().flowOn(singleCoroutineDispatcher).collect {
                Timber.d("########## getRenderUiChannel()")
                mCurrentState = it
                Timber.d("state: $it")
                mView?.render(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
//        if (mCurrentState is RenderState.VersusState) {
//            val renderState = mCurrentState as RenderState.VersusState
//            outState?.putString(FIRST_IMAGE_DESCRIPTION, renderState.firstImgDescription)
//            outState?.putString(SECOND_IMAGE_DESCRIPTION, renderState.secondImgDescription)
//            outState?.putInt(FIRST_IMAGE_RES_ID, renderState.firstImgResId)
//            outState?.putInt(SECOND_IMAGE_RES_ID, renderState.secondImgResId)
//        }
    }

    override fun onRestoreInstanceState(outState: Bundle?) {
//        val firstImgResId = outState?.getInt(FIRST_IMAGE_RES_ID, INVALID_VALUE) ?: INVALID_VALUE
//        val firstImgDescription = outState?.getString(FIRST_IMAGE_DESCRIPTION, EMPTY_STRING) ?: EMPTY_STRING
//        val secondImgResId = outState?.getInt(SECOND_IMAGE_RES_ID, INVALID_VALUE) ?: INVALID_VALUE
//        val secondImgDescription = outState?.getString(SECOND_IMAGE_DESCRIPTION, EMPTY_STRING) ?: EMPTY_STRING
//        if (firstImgResId != INVALID_VALUE
//            && firstImgDescription != EMPTY_STRING
//            && secondImgResId != INVALID_VALUE
//            && secondImgDescription != EMPTY_STRING) {
//            mView?.render(RenderState.VersusState(firstImgResId, firstImgDescription, secondImgResId, secondImgDescription))
//        }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }

    override fun onFirstImgClicked() {
        if (mCurrentState is RenderState.VersusState) {
            val state = mCurrentState as RenderState.VersusState
            mChosenContestantInteractor.onChosenContestant(state, true)
        } else if (mCurrentState is RenderState.WinnerState) {
            resetContest()
        }
    }

    override fun onSecondImgClicked() {
        if (mCurrentState is RenderState.VersusState) {
            val state = mCurrentState as RenderState.VersusState
            mChosenContestantInteractor.onChosenContestant(state, false)
        } else if (mCurrentState is RenderState.WinnerState) {
            resetContest()
        }
    }

    private fun resetContest() {
        mView?.resetViews()
        mResetInteractor.resetContest()
    }
}