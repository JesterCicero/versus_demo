package com.avp.ctbo.presentation.licenses

import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface ILicensesDialogContract {

    interface ILicensesDialogView: IBaseView {
        fun dismissDialog()
    }

    interface ILicensesDialogPresenter : IBaseStatefulPresenter<ILicensesDialogView> {
        fun onLicensesDialogFragmentDismissIntent()
    }
}