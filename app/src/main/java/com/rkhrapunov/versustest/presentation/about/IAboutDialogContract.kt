package com.rkhrapunov.versustest.presentation.about

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IAboutDialogContract {

    interface IAboutDialogView: IBaseView {
        fun dismissDialog()
    }

    interface IAboutDialogPresenter : IBaseStatefulPresenter<IAboutDialogView> {
        fun onAboutDialogFragmentDismissIntent()
    }
}