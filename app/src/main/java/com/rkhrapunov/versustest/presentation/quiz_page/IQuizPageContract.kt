package com.rkhrapunov.versustest.presentation.quiz_page

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IQuizPageContract {
    interface IQuizPageView : IBaseView
    interface IQuizPagePresenter : IBaseStatefulPresenter<IQuizPageView>
}