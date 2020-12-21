package com.avp.ctbo.presentation.about

import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IAboutDialogContract {

    interface IAboutDialogView: IBaseView {
        fun dismissDialog()
    }

    interface IAboutDialogPresenter : IBaseStatefulPresenter<IAboutDialogView> {
        fun onAboutDialogFragmentDismissIntent()
    }
}