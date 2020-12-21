package com.avp.ctbo.presentation.empty_pager

import com.avp.core.domain.RenderState
import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IEmptyPagerContract {

    interface IEmptyPagerView : IBaseView {
        suspend fun renderErrorState(renderState: RenderState.ErrorState)
        fun showToast()
    }

    interface IEmptyPagerPresenter : IBaseStatefulPresenter<IEmptyPagerView> {
        fun onRetryLoadingClickedIntent()
    }
}