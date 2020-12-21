package com.avp.ctbo.presentation.main

import com.avp.core.data.ISuperCategory
import com.avp.ctbo.presentation.base.IBaseStatefulPresenter
import com.avp.ctbo.presentation.base.IBaseView

interface IMainContract {
    interface IMainView : IBaseView {
        fun renderErrorState(errorMsg: String)
        fun onSuperCategoriesBack()
        fun onSuperCategoryChanged(previousPosition: Int, currentPosition: Int)
        fun updateSuperCategoryOnError()
    }
    interface IMainPresenter : IBaseStatefulPresenter<IMainView> {
        fun getSuperCategories()
        fun getSuperCategoriesFromCache(): List<ISuperCategory>?
        fun cancelQuiz()
        fun onInitialSuperCategory()
        fun updateSuperCategoryPosition(newPosition: Int)
        fun updateSuperCategoryOnError()
        fun resetResultsStatsOption()
        fun getCurrentSuperCategoryPosition(): Int
    }
}