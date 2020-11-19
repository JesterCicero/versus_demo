package com.rkhrapunov.versustest.presentation.main

import com.rkhrapunov.core.data.ISuperCategory
import com.rkhrapunov.versustest.presentation.base.IBaseStatefulPresenter
import com.rkhrapunov.versustest.presentation.base.IBaseView

interface IMainContract {
    interface IMainView : IBaseView {
        fun renderErrorState(errorMsg: String)
        fun onSuperCategoriesBack()
        fun onSuperCategoryChanged(previousPosition: Int, currentPosition: Int)
    }
    interface IMainPresenter : IBaseStatefulPresenter<IMainView> {
        fun getSuperCategories()
        fun getSuperCategoriesFromCache(): List<ISuperCategory>?
        fun cancelQuiz()
        fun onInitialSuperCategory()
        fun updateSuperCategoryPosition(newPosition: Int)
        fun resetResultsStatsOption()
        fun getCurrentSuperCategoryPosition(): Int
    }
}