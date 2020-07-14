package com.rkhrapunov.versustest.presentation.winner

import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IWinnerContract {

    interface IWinnerView : IBaseView {
        suspend fun renderWinnerState(renderState: IRenderState)
    }

    interface IWinnerPresenter : IBaseStatefulPresenter<IWinnerView> {
        fun onWinnerClickedIntent()
        fun onViewStatsClickedIntent()
    }
}