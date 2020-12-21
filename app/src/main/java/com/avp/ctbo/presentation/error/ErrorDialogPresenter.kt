package com.avp.ctbo.presentation.error

import com.avp.ctbo.presentation.base.BasePresenter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent

@FlowPreview
@ExperimentalCoroutinesApi
class ErrorDialogPresenter : BasePresenter<IErrorDialogContract.IErrorDialogView>(),
    IErrorDialogContract.IErrorDialogPresenter, KoinComponent {

    override fun onErrorDialogFragmentDismissIntent() {
        mView?.dismissDialog()
    }
}