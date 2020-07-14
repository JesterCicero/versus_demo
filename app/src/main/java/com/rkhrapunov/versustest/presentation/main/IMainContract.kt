package com.rkhrapunov.versustest.presentation.main

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IMainContract {
    interface IMainView : IBaseView
    interface IMainPresenter : IBaseStatefulPresenter<IMainView> {
        fun cancelQuiz()
    }
}