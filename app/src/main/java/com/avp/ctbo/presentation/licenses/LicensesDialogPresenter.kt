package com.avp.ctbo.presentation.licenses

import com.avp.ctbo.presentation.base.BasePresenter
import org.koin.core.component.KoinComponent

class LicensesDialogPresenter : BasePresenter<ILicensesDialogContract.ILicensesDialogView>(),
    ILicensesDialogContract.ILicensesDialogPresenter, KoinComponent {

    override fun onLicensesDialogFragmentDismissIntent() {
        mView?.dismissDialog()
    }
}