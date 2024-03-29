package com.avp.core.domain

import com.avp.core.data.ISuperCategory
import com.avp.core.data.ICategory
import com.avp.core.data.IQuizShortInfo
import com.avp.core.data.IContestantsInfo
import com.avp.core.data.IContestantsStatsInfo

sealed class RenderState : IRenderState {

    data class SuperCategoriesState(val superCategories: List<ISuperCategory>) : RenderState()

    data class CategoriesState(val categories: List<ICategory>) : RenderState()

    data class QuizListState(val allContestants: List<IQuizShortInfo>) : RenderState()

    data class QuizItemDetailState(val firstContestant: IContestantsInfo,
                                   val secondContestant: IContestantsInfo,
                                   val round: String,
                                   val quizTitle: String,
                                   val quizDescription: String) : RenderState()

    data class StatsListState(val statsContestants: List<IContestantsStatsInfo>, val top4List: List<IContestantsInfo>) : RenderState()

    data class WinnerState(val winner: IContestantsInfo): RenderState()

    data class WinnerFinalState(val winner: IContestantsInfo) : RenderState()

    data class ErrorState(val errorMsg: String) : RenderState()
}