package com.rkhrapunov.versustest.framework

import com.rkhrapunov.core.data.IContestantsDataSource
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

@ExperimentalCoroutinesApi
class ContestantsDataSource : IContestantsDataSource, KoinComponent {

    private val mRenderUiChannel by inject<ConflatedBroadcastChannel<IRenderState>>(named("RenderState"))
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mAllContestants = mapOf(
        "Charlize Theron" to R.drawable.charlize_theron,
        "Jennifer Lawrence" to R.drawable.jennifer_lawrence,
        "Margot Robbie" to R.drawable.margot_robbie,
        "Megan Fox" to R.drawable.megan_fox,
        "Mila Kunis" to R.drawable.mila_kunis,
        "Monica Bellucci" to R.drawable.monica_bellucci,
        "Natalie Portman" to R.drawable.natalie_portman,
        "Scarlett Johansson" to R.drawable.scarlett_johansson
    )

    private val mCurrentContestants = mAllContestants.toMutableMap()
    private val mWinners = mutableMapOf<String, Int>()
    private var mCurrentRound = mCurrentContestants.size / 2

    init {
        mCoroutineLauncherHelper.launch {
            mRenderUiChannel.send(getVersusState())
        }
    }

    override fun getAllContestants() = mAllContestants

    @ExperimentalCoroutinesApi
    override fun getRenderUiChannel() = mRenderUiChannel

    override fun updateState(state: IRenderState, chosenFirst: Boolean) {
        if (state is RenderState.VersusState) {
            if (chosenFirst) {
                mWinners[state.firstImgDescription] = state.firstImgResId
            } else {
                mWinners[state.secondImgDescription] = state.secondImgResId
            }
        }
        if (mCurrentContestants.isEmpty()) {
            mCurrentRound /= 2
            mCurrentContestants.putAll(mWinners)
            if (mCurrentRound != 0) {
                mWinners.clear()
            }
        }
        val resultState = if (mCurrentRound == 0) {
            val list = mWinners.toList()
            mWinners.clear()
            RenderState.WinnerState(list[0].second, list[0].first,  chosenFirst)
        } else {
            getVersusState()
        }
        mCoroutineLauncherHelper.launch {
            mRenderUiChannel.send(resultState)
        }
    }

    override fun resetContest() {
        mCurrentContestants.putAll(mAllContestants)
        mWinners.clear()
        mCurrentRound = mCurrentContestants.size / 2
        mCoroutineLauncherHelper.launch {
            mRenderUiChannel.send(getVersusState())
        }
    }

    private fun getVersusState(): RenderState.VersusState {
        val firstEntry = mCurrentContestants.random()
        mCurrentContestants.remove(firstEntry.key)
        val secondEntry = mCurrentContestants.random()
        mCurrentContestants.remove(secondEntry.key)
        return RenderState.VersusState(firstEntry.value,
            firstEntry.key,
            secondEntry.value,
            secondEntry.key,
            if (mCurrentRound == 1) "Final" else "1/$mCurrentRound")
    }
}