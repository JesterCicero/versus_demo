package com.avp.ctbo.presentation.winner

import com.avp.core.domain.IRenderState
import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IWinnerContract {

    interface IWinnerView : IBaseView {
        suspend fun renderWinnerState(renderState: IRenderState)
        fun hideProgressBar()
    }

    interface IWinnerPresenter : IBaseStatefulPresenter<IWinnerView> {
        fun onWinnerClickedIntent()
        fun onViewStatsClickedIntent()
        fun getCurrentQuizBackgroundUrl(): String
    }
}