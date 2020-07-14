package com.rkhrapunov.versustest.framework

import com.rkhrapunov.core.data.IContestantsDataSource
import com.rkhrapunov.core.data.IContestantsInfo
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.framework.helpers.RestApiHelper
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import retrofit2.Response
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class ContestantsDataSource : IContestantsDataSource, KoinComponent {

    private val mRenderUiChannel by inject<ConflatedBroadcastChannel<IRenderState>>(named("RenderState"))
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRestApiHelper by inject<RestApiHelper>()
    private val mContestantsCache by inject<ContestantsCache>()
    private var mCurrentContestants = listOf<IContestantsInfo>()
    private var mInterimContestants = mutableListOf<IContestantsInfo>()
    private var mWinnersList = mutableListOf<IContestantsInfo>()
    private var mCurrentQuiz = EMPTY_STRING
    private var mCurrentRound = 0
    private var mLastWinnerState: RenderState.WinnerState? = null
    private var mStartQuizJob: Job? = null
    private var mUpdateStateJob: Job? = null
    private var mPostWinnerJob: Job? = null
    private var mResetContestUpdateUiJob: Job? = null
    private var mGetQuizListJob: Job? = null
    private var mGetQuizItemDetailJob: Job? = null
    private var mGetStatsJob: Job? = null
    private var mQuizListUpdateUiJob: Job? = null
    private var mQuizStatsUpdateUiJob: Job? = null
    private var mPostWinnerSuccessUpdateUiJob: Job? = null

    companion object {
        private const val WINNER_RESPONSE = "Got your request!"
    }

    @ExperimentalCoroutinesApi
    override fun getRenderUiChannel() = mRenderUiChannel

    override fun updateState(state: IRenderState, chosenFirst: Boolean) {
        mUpdateStateJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            if (state is RenderState.QuizItemDetailState) {
                mWinnersList.add(if (chosenFirst) state.firstContestant else state.secondContestant)
            }
            if (mInterimContestants.isEmpty()) {
                mCurrentRound /= 2
                Timber.d("current round: $mCurrentRound")
                mInterimContestants.addAll(mWinnersList)
                if (mCurrentRound != 0) {
                    mWinnersList.clear()
                }
            }
            try {
                val resultState = if (mCurrentRound == 0) {
                    val winnerState = RenderState.WinnerState(mWinnersList[0])
                    val winnerName = mWinnersList[0].name
                    mPostWinnerJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({ postWinner(winnerName) })
                    mWinnersList.clear()
                    mLastWinnerState = winnerState
                    winnerState
                } else {
                    getQuizItemDetailState()
                }
                mRenderUiChannel.send(resultState)
            } catch (e: Exception) {
                Timber.e("Exception caught: $e")
            }
        }
    }

    override fun resetContest() {
        mInterimContestants.clear()
        mWinnersList.clear()
        mInterimContestants.addAll(mCurrentContestants)
        mCurrentRound = mInterimContestants.size / 2
        mResetContestUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            try {
                mRenderUiChannel.send(getQuizItemDetailState())
            } catch (e: Exception) {
                Timber.e("Exception caught: $e")
            }
        }
    }

    override fun getQuizList() {
        mGetQuizListJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
            mContestantsCache.tryToGetQuizzesInfoCache()
            getAllQuizzesApi()
        })
    }

    override fun getQuizItemDetail(itemData: String) {
        mCurrentQuiz = itemData
        mGetQuizItemDetailJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
            mContestantsCache.tryToGetQuizInfoCache(mCurrentQuiz)?.let {
                Timber.d("starting quiz with values from cache")
                startQuiz(it)
            }
            getQuizApi()
        })
    }

    override fun getStats() {
        mGetStatsJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({ getQuizStatsApi() })
    }

    override fun sendWinner(winner: String) = postWinner(winner)

    override fun cancelQuiz() {
        Timber.d("cancelQuiz()")
        mStartQuizJob?.cancel()
        mUpdateStateJob?.cancel()
        mPostWinnerJob?.cancel()
        mResetContestUpdateUiJob?.cancel()
        mGetQuizListJob?.cancel()
        mGetQuizItemDetailJob?.cancel()
        mGetStatsJob?.cancel()
        mQuizListUpdateUiJob?.cancel()
        mQuizStatsUpdateUiJob?.cancel()
        mPostWinnerSuccessUpdateUiJob?.cancel()
        clearData()
    }

    private fun clearData() {
        mInterimContestants.clear()
        mCurrentContestants = emptyList()
        mWinnersList.clear()
    }

    private fun getQuizApi() {
        Timber.d("getQuizApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.QUIZ,
            { onGetQuizApiSuccess(it as Response<List<ContestantsInfo>>) },
            { Timber.d(it, "Error occurred while getting request for quiz!") },
            mCurrentQuiz
        )
    }

    private fun onGetQuizApiSuccess(response: Response<List<ContestantsInfo>>) {
        response.body()?.let {
            val sortedQuizList: List<IContestantsInfo> = it.sortedBy { element -> element.name }
            if (mContestantsCache.isQuizCacheEmpty(mCurrentQuiz)) {
                mContestantsCache.updateQuizInfoCache(sortedQuizList)
                startQuiz(sortedQuizList)
            } else {
                mContestantsCache.quizCache?.let { quizCacheList ->
                    if (quizCacheList != sortedQuizList) {
                        Timber.d("quiz was updated from server")
                        mContestantsCache.updateQuizInfoCache(sortedQuizList)
                        getQuizList()
                    }
                }
            }
        }
    }

    private fun startQuiz(contestants: List<IContestantsInfo>) {
        mUpdateStateJob?.cancel()
        mResetContestUpdateUiJob?.cancel()
        clearData()
        mStartQuizJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mCurrentContestants = contestants
            mInterimContestants = mCurrentContestants.toMutableList()
            Timber.d("interim contestants size: ${mInterimContestants.size}")
            mCurrentRound = mInterimContestants.size / 2
            try {
                mRenderUiChannel.send(getQuizItemDetailState())
            } catch (e: Exception) {
                Timber.e("Exception caught: $e")
            }
        }
    }

    private fun getAllQuizzesApi() {
        Timber.d("getAllQuizzesApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.ALL_QUIZZES,
            { onGetAllQuizzesApiSuccess(it as Response<List<QuizShortInfo>>) },
            { Timber.d(it, "Error occurred while getting response for quizzes list!") }
        )
    }

    private fun onGetAllQuizzesApiSuccess(response: Response<List<QuizShortInfo>>) {
        response.body()?.let {
            Timber.d("onGetAllQuizzesApiSuccess(): allQuizzes size: ${it.size}")
            val sortedQuizShortInfoList = it.sortedBy { element -> element.title }
            val listsUnequal = mContestantsCache.quizzesInfoCache != sortedQuizShortInfoList
            Timber.d("listsUnequal: $listsUnequal")
            if (listsUnequal) {
                Timber.d("onGetAllQuizzesApiSuccess(): got new quizzes from server")
                mContestantsCache.updateQuizzesInfoCache(sortedQuizShortInfoList)
                mQuizListUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mRenderUiChannel.send(RenderState.QuizListState(sortedQuizShortInfoList))
                }
            }
        }
    }

    private fun getQuizStatsApi() {
        Timber.d("getQuizStatsApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.QUIZ_STATS,
            { onGetQuizStatsApiSuccess(it as Response<List<ContestantsStatsInfo>>) },
            { Timber.d(it, "Error occurred while getting request for contestants stats!") },
            mCurrentQuiz
        )
    }

    private fun onGetQuizStatsApiSuccess(response: Response<List<ContestantsStatsInfo>>) {
        response.body()?.let {
            Timber.d("onGetQuizStatsApiSuccess(): quiz stats size: ${it.size}")
            mQuizStatsUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mRenderUiChannel.send(RenderState.StatsListState(it))
            }
        }
    }

    private fun postWinner(winner: String) {
        Timber.d("postWinner()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.WINNER,
            { onPostWinnerSuccess(it as Response<String>) },
            { Timber.d(it, "Error occurred while getting response for sending winner!") },
            "$winner;$mCurrentQuiz"
        )
    }

    private fun onPostWinnerSuccess(response: Response<String>) {
        response.body()?.let {
            if (it == WINNER_RESPONSE) {
                mPostWinnerSuccessUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mLastWinnerState?.let { lastWinnerState ->
                        mRenderUiChannel.send(RenderState.WinnerFinalState(lastWinnerState.winner))
                    }
                }
            }
        }
    }

    private fun getQuizItemDetailState(): RenderState.QuizItemDetailState {
        val firstContestant = mInterimContestants.random()
        mInterimContestants.remove(firstContestant)
        val secondContestant = mInterimContestants.random()
        mInterimContestants.remove(secondContestant)
        return RenderState.QuizItemDetailState(
            firstContestant, secondContestant, if (mCurrentRound == 1) "Final" else "1/$mCurrentRound")
    }
}