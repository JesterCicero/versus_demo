package com.avp.ctbo.presentation.quiz_detail

import com.avp.core.data.ChosenContestant
import com.avp.core.domain.RenderState
import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IQuizItemDetailContract {

    interface IQuizItemDetailView : IBaseView {
        fun renderQuizItemDetailState(renderState: RenderState.QuizItemDetailState, shouldLoadBackground: Boolean)
        fun onItemClicked(chosenFirst: Boolean)
        fun onItemChosen(chosenFirst: Boolean)
    }

    interface IQuizItemDetailPresenter : IBaseStatefulPresenter<IQuizItemDetailView> {
        fun onFirstImgClickedIntent()
        fun onSecondImgClickedIntent()
        fun onNextButtonClickedIntent()
        fun onItemClickFinished(chosenFirst: Boolean)
        fun retrieveChosenContestant(): ChosenContestant
        fun saveChosenContestant(chosenContestant: ChosenContestant)
        fun getCurrentQuizBackgroundUrl(): String
        fun getCurrentRound(): Int
    }
}