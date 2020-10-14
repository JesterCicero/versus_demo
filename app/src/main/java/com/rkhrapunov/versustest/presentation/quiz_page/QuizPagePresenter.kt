package com.rkhrapunov.versustest.presentation.quiz_page

import com.rkhrapunov.core.interactors.GetCategoriesInteractor
import com.rkhrapunov.core.interactors.GetQuizItemDetailInteractor
import com.rkhrapunov.core.interactors.GetQuizListInteractor
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import com.rkhrapunov.versustest.presentation.base.IItemClickListener
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class QuizPagePresenter : BasePresenter<IQuizPageContract.IQuizPageView>(),
    IQuizPageContract.IQuizPagePresenter, KoinComponent,
    IItemClickListener {

    private val mGetCategoriesInteractor by inject<GetCategoriesInteractor>()
    private val mGetQuizListInteractor by inject<GetQuizListInteractor>()
    private val mGetQuizItemDetailInteractor by inject<GetQuizItemDetailInteractor>()

    override fun onItemClickedIntent(itemData: String, position: Int) {
        mView?.let {
            val selectedDataType = it.getSelectedDataType()
            Timber.d("selected data type: $selectedDataType")
            when (selectedDataType) {
                SelectedData.SUPER_CATEGORY -> mGetCategoriesInteractor.getCategories(itemData)
                SelectedData.CATEGORY -> mGetQuizListInteractor.getQuizList(itemData)
                SelectedData.QUIZ -> mGetQuizItemDetailInteractor.getQuizItemDetail(itemData)
                else -> Timber.w("Invalid selected data type: $selectedDataType")
            }
        }
    }
}