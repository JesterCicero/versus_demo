package com.rkhrapunov.versustest.presentation.error

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IErrorDialogContract {

    interface IErrorDialogView: IBaseView {
        fun dismissRecognitionDialog()
    }

    interface IErrorDialogPresenter : IBaseStatefulPresenter<IErrorDialogView> {
        fun onErrorDialogFragmentDismissIntent()
    }
}