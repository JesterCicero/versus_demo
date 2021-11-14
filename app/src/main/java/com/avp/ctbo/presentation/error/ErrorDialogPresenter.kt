package com.avp.ctbo.presentation.error

import com.avp.ctbo.presentation.base.BasePresenter
import org.koin.core.component.KoinComponent

class ErrorDialogPresenter : BasePresenter<IErrorDialogContract.IErrorDialogView>(),
    IErrorDialogContract.IErrorDialogPresenter, KoinComponent {

    override fun onErrorDialogFragmentDismissIntent() {
        mView?.dismissDialog()
    }
}