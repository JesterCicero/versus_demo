package com.rkhrapunov.core.domain

sealed class RenderState : IRenderState {
    data class VersusState(val firstImgResId: Int,
                           val firstImgDescription: String,
                           val secondImgResId: Int,
                           val secondImgDescription: String,
                           val round: String): RenderState()

    data class WinnerState(val winnerImgResId: Int, val winnerImgDescription: String, val firstContestantWon: Boolean): RenderState()
}