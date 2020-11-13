package com.rkhrapunov.versustest.presentation.licenses

import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface ILicensesDialogContract {

    interface ILicensesDialogView: IBaseView {
        fun dismissDialog()
    }

    interface ILicensesDialogPresenter : IBaseStatefulPresenter<ILicensesDialogView> {
        fun onLicensesDialogFragmentDismissIntent()
    }
}