package com.avp.ctbo.presentation.quiz_page

import com.avp.core.interactors.GetCategoriesInteractor
import com.avp.core.interactors.GetQuizItemDetailInteractor
import com.avp.core.interactors.GetQuizListInteractor
import com.avp.ctbo.presentation.base.BasePresenter
import com.avp.ctbo.presentation.base.IItemClickListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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