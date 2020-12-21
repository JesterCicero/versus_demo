package com.avp.ctbo.presentation.quiz_detail

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.avp.core.data.ChosenContestant
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.ChosenContestantInteractor
import com.avp.core.interactors.GetCurrentQuizInteractor
import com.avp.core.interactors.GetCurrentRoundInteractor
import com.avp.core.interactors.RetrieveChosenContestantInteractor
import com.avp.ctbo.framework.ContestantsCache
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.BasePresenter
import com.avp.ctbo.presentation.base.Constants
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
    private val mGetCurrentQuizInteractor by inject<GetCurrentQuizInteractor>()
    private val mGetCurrentRoundInteractor by inject<GetCurrentRoundInteractor>()
    private val mContestantsCache by inject<ContestantsCache>()
    private var mJob: Job? = null
    private var mCurrentState: IRenderState? = null
    private var mQuizItemStateUpdated = false
    private val mRetrieveChosenContestantInteractor by inject<RetrieveChosenContestantInteractor>()

    override fun attachView(view: IQuizItemDetailContract.IQuizItemDetailView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        Timber.d("attachView()")
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .filter {
                    Timber.d("filter(): current state=$mCurrentState, new state=$it")
                    it is RenderState.QuizItemDetailState && it != mCurrentState
                }
                .collect { renderState ->
                    Timber.d("attachView(): renderState=$renderState")
                    if (renderState is RenderState.QuizItemDetailState) {
                        mQuizItemStateUpdated = true
                        mView?.renderQuizItemDetailState(renderState, mCurrentState == null)
                        mCurrentState = renderState
                    }
                }
        }
    }

    override fun onFirstImgClickedIntent() {
        val chosenContestant = mRetrieveChosenContestantInteractor.getChosenContestant()
        if (chosenContestant == ChosenContestant.UNKNOWN
            || chosenContestant == ChosenContestant.CHOSEN_SECOND) {
            mView?.onItemChosen(true)
        }
    }

    override fun onSecondImgClickedIntent() {
        val chosenContestant = mRetrieveChosenContestantInteractor.getChosenContestant()
        if (chosenContestant == ChosenContestant.UNKNOWN
            || chosenContestant == ChosenContestant.CHOSEN_FIRST) {
            mView?.onItemChosen(false)
        }
    }

    override fun onNextButtonClickedIntent() {
        Timber.d("onNextButtonClickedIntent()")
        val chosenContestant = mRetrieveChosenContestantInteractor.getChosenContestant()
        if (chosenContestant != ChosenContestant.UNKNOWN) {
            onItemClicked(chosenContestant == ChosenContestant.CHOSEN_FIRST)
            mRetrieveChosenContestantInteractor.saveChosenContestant(ChosenContestant.UNKNOWN)
        }
    }

    override fun getCurrentRound() = mGetCurrentRoundInteractor.getCurrentRound()

    override fun onItemClickFinished(chosenFirst: Boolean) {
        mCurrentState?.let { mChosenContestantInteractor.onChosenContestant(it, chosenFirst) }
    }

    override fun retrieveChosenContestant() = mRetrieveChosenContestantInteractor.getChosenContestant()

    override fun saveChosenContestant(chosenContestant: ChosenContestant) = mRetrieveChosenContestantInteractor.saveChosenContestant(chosenContestant)

    override fun getCurrentQuizBackgroundUrl(): String {
        val currentQuiz = mGetCurrentQuizInteractor.getCurrentQuiz()
        Timber.d("getCurrentQuizBackgroundUrl(): current quiz: $currentQuiz")
        val filteredQuizzes = mContestantsCache.quizzesInfoCache?.filter {
            it.title == mGetCurrentQuizInteractor.getCurrentQuiz()
        }
        Timber.d("getCurrentQuizBackgroundUrl(): filteredQuizzes list size: ${filteredQuizzes?.size}")
        return filteredQuizzes?.let {
            if (it.isNotEmpty()) it[0].backgroundUrl else Constants.EMPTY_STRING
        } ?: Constants.EMPTY_STRING
    }

    private fun onItemClicked(chosenFirst: Boolean) {
        if (mQuizItemStateUpdated) {
            mQuizItemStateUpdated = false
            mView?.onItemClicked(chosenFirst)
        }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }
}