package com.avp.ctbo.presentation.error

import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IErrorDialogContract {

    interface IErrorDialogView: IBaseView {
        fun dismissDialog()
    }

    interface IErrorDialogPresenter : IBaseStatefulPresenter<IErrorDialogView> {
        fun onErrorDialogFragmentDismissIntent()
    }
}