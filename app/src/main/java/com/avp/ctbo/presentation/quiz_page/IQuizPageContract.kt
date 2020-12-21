package com.avp.ctbo.presentation.quiz_page

import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IQuizPageContract {
    interface IQuizPageView : IBaseView {
        fun getSelectedDataType(): SelectedData
    }
    interface IQuizPagePresenter : IBaseStatefulPresenter<IQuizPageView>
}