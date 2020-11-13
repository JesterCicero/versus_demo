package com.rkhrapunov.versustest.presentation.about

import com.rkhrapunov.versustest.presentation.base.BasePresenter
import org.koin.core.KoinComponent

class AboutDialogPresenter : BasePresenter<IAboutDialogContract.IAboutDialogView>(),
    IAboutDialogContract.IAboutDialogPresenter, KoinComponent {

    override fun onAboutDialogFragmentDismissIntent() {
        mView?.dismissDialog()
    }
}