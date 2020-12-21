package com.avp.ctbo.presentation.about

import com.avp.ctbo.presentation.base.BasePresenter
import org.koin.core.KoinComponent

class AboutDialogPresenter : BasePresenter<IAboutDialogContract.IAboutDialogView>(),
    IAboutDialogContract.IAboutDialogPresenter, KoinComponent {

    override fun onAboutDialogFragmentDismissIntent() {
        mView?.dismissDialog()
    }
}