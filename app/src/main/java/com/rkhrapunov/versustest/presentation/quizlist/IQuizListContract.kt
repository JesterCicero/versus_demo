package com.rkhrapunov.versustest.presentation.quizlist

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IQuizListContract {

    interface IQuizListView : IBaseView {
        fun shouldShowQuizList(): Boolean
        fun setAdapter(quizListAdapter: QuizListAdapter<*>?)
        fun onBackToQuizzesButtonClicked()
    }

    interface IQuizListPresenter : IBaseStatefulPresenter<IQuizListView> {
        fun onBackToQuizzesButtonClickedIntent()
    }
}