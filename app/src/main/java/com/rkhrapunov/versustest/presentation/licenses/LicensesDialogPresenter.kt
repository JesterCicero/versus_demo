package com.rkhrapunov.versustest.presentation.licenses

import com.rkhrapunov.versustest.presentation.base.BasePresenter
import org.koin.core.KoinComponent

class LicensesDialogPresenter : BasePresenter<ILicensesDialogContract.ILicensesDialogView>(),
    ILicensesDialogContract.ILicensesDialogPresenter, KoinComponent {

    override fun onLicensesDialogFragmentDismissIntent() {
        mView?.dismissDialog()
    }
}