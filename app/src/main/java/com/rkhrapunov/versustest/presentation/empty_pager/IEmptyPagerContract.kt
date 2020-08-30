package com.rkhrapunov.versustest.presentation.empty_pager

import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IEmptyPagerContract {

    interface IEmptyPagerView : IBaseView {
        suspend fun renderErrorState(renderState: RenderState.ErrorState)
        fun showToast()
    }

    interface IEmptyPagerPresenter : IBaseStatefulPresenter<IEmptyPagerView> {
        fun onRetryLoadingClickedIntent()
    }
}