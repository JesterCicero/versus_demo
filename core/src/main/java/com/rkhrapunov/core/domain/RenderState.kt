package com.rkhrapunov.core.domain

import com.rkhrapunov.core.data.IContestantsInfo
import com.rkhrapunov.core.data.IContestantsStatsInfo
import com.rkhrapunov.core.data.IQuizShortInfo

sealed class RenderState : IRenderState {

    data class QuizListState(val allContestants: List<IQuizShortInfo>) : RenderState()

    data class QuizItemDetailState(val firstContestant: IContestantsInfo, val secondContestant: IContestantsInfo, val round: String) : RenderState()

    data class StatsListState(val statsContestants: List<IContestantsStatsInfo>) : RenderState()

    data class WinnerState(val winner: IContestantsInfo): RenderState()

    data class WinnerFinalState(val winner: IContestantsInfo) : RenderState()
}