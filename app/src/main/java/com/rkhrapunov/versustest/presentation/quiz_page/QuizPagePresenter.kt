package com.rkhrapunov.versustest.presentation.quiz_page

import com.rkhrapunov.core.interactors.GetQuizItemDetailInteractor
import com.rkhrapunov.versustest.presentation.base.BasePresenter
import com.rkhrapunov.versustest.presentation.quizlist.IItemClickListener
import org.koin.core.KoinComponent
import org.koin.core.inject

class QuizPagePresenter : BasePresenter<IQuizPageContract.IQuizPageView>(),
    IQuizPageContract.IQuizPagePresenter, KoinComponent, IItemClickListener {

    private val mGetQuizItemDetailInteractor by inject<GetQuizItemDetailInteractor>()

    override fun onItemClickedIntent(itemData: String) = mGetQuizItemDetailInteractor.getQuizItemDetail(itemData)
}