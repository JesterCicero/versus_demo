package com.avp.ctbo.framework

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.avp.core.data.IContestantsDataSource
import com.avp.core.data.IContestantsInfo
import com.avp.core.data.ChosenContestant
import com.avp.core.data.IQuizShortInfo
import com.avp.core.data.ICategory
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.ctbo.R
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.framework.helpers.RestApiHelper
import com.avp.ctbo.presentation.base.Constants.EMPTY_STRING
import com.avp.ctbo.presentation.base.Constants.INVALID_VALUE
import com.avp.ctbo.presentation.base.Constants.MAX_TOP_4_ITEMS
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import retrofit2.Response
import timber.log.Timber
import java.util.Locale

@DelicateCoroutinesApi
@Suppress("UNCHECKED_CAST")
class ContestantsDataSource : IContestantsDataSource, KoinComponent {

    private val mRenderUiChannel by inject<MutableStateFlow<IRenderState>>(named("RenderState"))
    private val mErrorMsgChannel by inject<MutableSharedFlow<String>>(named("ErrorMsg"))
    private val mNetworkStateChannel by inject<MutableSharedFlow<Boolean>>(named("NetworkState"))
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRestApiHelper by inject<RestApiHelper>()
    private val mContestantsCache by inject<ContestantsCache>()
    private var mCurrentContestants = listOf<IContestantsInfo>()
    private var mInterimContestants = mutableListOf<IContestantsInfo>()
    private var mWinnersList = mutableListOf<IContestantsInfo>()
    private var mTop4Array = arrayOfNulls<IContestantsInfo>(MAX_TOP_4_ITEMS)
    private var mCurrentSuperCategory = EMPTY_STRING
    private var mCurrentCategory = EMPTY_STRING
    private var mCurrentQuiz = EMPTY_STRING
    private var mCurrentRound = 0
    private var mLastWinnerState: RenderState.WinnerState? = null
    private var mStartQuizJob: Job? = null
    private var mUpdateStateJob: Job? = null
    private var mPostWinnerJob: Job? = null
    private var mResetContestUpdateUiJob: Job? = null
    private var mGetSuperCategoriesJob: Job? = null
    private var mGetCategoriesJob: Job? = null
    private var mGetQuizListJob: Job? = null
    private var mGetAllQuizzesJob: Job? = null
    private var mGetQuizItemDetailJob: Job? = null
    private var mGetStatsJob: Job? = null
    private var mSuperCategoriesUpdateUiJob: Job? = null
    private var mCategoriesUpdateUiJob: Job? = null
    private var mQuizListUpdateUiJob: Job? = null
    private var mQuizStatsUpdateUiJob: Job? = null
    private var mPostWinnerSuccessUpdateUiJob: Job? = null
    private var mUnableToGetSuperCategoriesJob: Job? = null
    private var mUnableToGetCategoriesJob: Job? = null
    private var mUnableToGetQuizListJob: Job? = null
    private var mUnableToGetQuizJob: Job? = null
    private var mUnableToGetStatsJob: Job? = null
    private var mQuizUpdatedOnServerJob: Job? = null
    private var mUnableToPostWinnerJob: Job? = null
    private var mPageIndicatorText: String = EMPTY_STRING
    private var mCurrentPagePosition: Int = 0
    private var mCurrentSuperCategoryPosition: Int = INVALID_VALUE
    private var mPreviousSuperCategoryPosition: Int = INVALID_VALUE
    private val mContext by inject<Context>()
    private val mDeferredWinnersList = mutableListOf<String>()
    private var mDeferredPost = false
    private var mChosenContestant = ChosenContestant.UNKNOWN
    private var mResultsStats = true
    private var mWinnerDetected = false
    private var mGetQuizApiFailed = false
    private lateinit var mFirstContestant: IContestantsInfo
    private lateinit var mSecondContestant: IContestantsInfo
    private var mCurrentQuizzesList = emptyList<IQuizShortInfo>()

    companion object {
        private const val WINNER_RESPONSE = "Got your request!"
        private const val POST_WINNER_SEPARATOR = ';'
        private const val POST_WINNER_ERROR_TIMEOUT = 1000L
    }

    init {
        mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
            mNetworkStateChannel
                .collect {
                    if (it) {
                        if (mDeferredWinnersList.isNotEmpty()) {
                            mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
                                Timber.d("trying to post winners")
                                mDeferredPost = true
                                mDeferredWinnersList.forEach { winner -> postWinner(winner) }
                            })
                        }
                    }
                }
        })
    }

    override fun getRenderUiChannel() = mRenderUiChannel

    override fun getErrorMsgChannel() = mErrorMsgChannel

    override fun updateState(state: IRenderState, chosenFirst: Boolean) {
        Timber.d("updateState()")
        mUpdateStateJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            if (state is RenderState.QuizItemDetailState) {
                onQuizItemDetailState(state, chosenFirst)
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
                    mWinnerDetected = true
                    winnerState
                } else {
                    getQuizItemDetailState()
                }
                mRenderUiChannel.emit(resultState)
            } catch (e: Exception) {
                Timber.e("Exception caught: $e")
            }
        }
    }

    private fun onQuizItemDetailState(state: RenderState.QuizItemDetailState, chosenFirst: Boolean) {
        val winner = if (chosenFirst) state.firstContestant else state.secondContestant
        val loser = if (chosenFirst) state.secondContestant else state.firstContestant
        mWinnersList.add(winner)
        if (mCurrentRound == 2) {
            Timber.d("loser: ${loser.name}")
            mTop4Array[3]?.let {
                Timber.d("adding ${loser.name} to 3rd place")
                mTop4Array[2] = loser
            } ?: run {
                Timber.d("adding ${loser.name} to 4th place")
                mTop4Array[3] = loser
            }
        } else if (mCurrentRound == 1) {
            mTop4Array[1] = loser
            mTop4Array[0] = winner
        }
    }

    override fun resetContest() {
        mInterimContestants.clear()
        mWinnersList.clear()
        mInterimContestants.addAll(mCurrentContestants)
        mCurrentRound = mInterimContestants.size / 2
        mResetContestUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            try {
                mRenderUiChannel.emit(getQuizItemDetailState())
            } catch (e: Exception) {
                Timber.e("Exception caught: $e")
            }
        }
    }

    override fun getSuperCategories() {
        mGetSuperCategoriesJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
            mContestantsCache.tryToGetSuperCategoriesCache()
            getSuperCategoriesApi()
        })
    }

    override fun getCategories(itemData: String) {
        if (itemData.isNotEmpty()) {
            mCurrentSuperCategory = itemData
            val allStr = mContext.getString(R.string.all)
            if (mCurrentSuperCategory == allStr) {
                mCurrentCategory = mCurrentSuperCategory
                mGetAllQuizzesJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
                    mContestantsCache.tryToGetQuizzesInfoCache(mCurrentCategory)
                    getAllQuizzesApi()
                })
                return
            }
        }
        mGetCategoriesJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
            mContestantsCache.tryToGetCategoriesCache(mCurrentSuperCategory)
            getCategoriesApi()
        })
    }

    override fun getQuizList(itemData: String) {
        mCurrentCategory = itemData
        getQuizzes()
    }

    private fun getQuizzes() {
        mGetQuizListJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
            if (mContestantsCache.tryToGetQuizzesInfoCache(mCurrentCategory)) {
                mContestantsCache.quizzesInfoCache?.let { mCurrentQuizzesList = it }
            }
            if (mCurrentCategory == mContext.getString(R.string.all)) {
                getAllQuizzesApi()
            } else {
                getQuizzesApi()
            }
        })
    }

    override fun getQuizItemDetail(itemData: String) {
        if (mCurrentQuiz == itemData && !mWinnerDetected && !mGetQuizApiFailed) {
            Timber.d("getQuizItemDetail(): current quiz: $mCurrentQuiz")
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mRenderUiChannel.emit(getQuizItemDetailState(true))
            }
        } else {
            mWinnerDetected = false
            mCurrentQuiz = itemData
            mGetQuizItemDetailJob = mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({
                mContestantsCache.tryToGetQuizInfoCache(mCurrentQuiz)?.let {
                    Timber.d("starting quiz with values from cache")
                    startQuiz(it)
                }
                getQuizApi()
            })
        }
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
        mGetSuperCategoriesJob?.cancel()
        mGetCategoriesJob?.cancel()
        mGetQuizListJob?.cancel()
        mGetAllQuizzesJob?.cancel()
        mGetQuizItemDetailJob?.cancel()
        mGetStatsJob?.cancel()
        mSuperCategoriesUpdateUiJob?.cancel()
        mCategoriesUpdateUiJob?.cancel()
        mQuizListUpdateUiJob?.cancel()
        mQuizStatsUpdateUiJob?.cancel()
        mPostWinnerSuccessUpdateUiJob?.cancel()
        mUnableToGetSuperCategoriesJob?.cancel()
        mUnableToGetCategoriesJob?.cancel()
        mUnableToGetQuizListJob?.cancel()
        mUnableToGetQuizJob?.cancel()
        mUnableToGetStatsJob?.cancel()
        mQuizUpdatedOnServerJob?.cancel()
        mUnableToPostWinnerJob?.cancel()
    }

    override fun savePageIndicatorText(pageIndicatorText: String) {
        mPageIndicatorText = pageIndicatorText
    }

    override fun getPageIndicatorText() = mPageIndicatorText

    override fun saveCurrentPagePosition(currentPosition: Int) {
        mCurrentPagePosition = currentPosition
    }

    override fun getCurrentPagePosition() = mCurrentPagePosition

    override fun saveCurrentSuperCategoryPosition(currentPosition: Int) {
        mPreviousSuperCategoryPosition = mCurrentSuperCategoryPosition
        mCurrentSuperCategoryPosition = currentPosition
    }

    override fun getCurrentSuperCategoryPosition() = mCurrentSuperCategoryPosition

    override fun getPreviousSuperCategoryPosition() = mPreviousSuperCategoryPosition

    override fun saveChosenContestant(chosenContestant: ChosenContestant) {
        mChosenContestant = chosenContestant
    }

    override fun getChosenContestant() = mChosenContestant

    override fun getCurrentSuperCategory() = mCurrentSuperCategory

    override fun getCurrentCategory() = mCurrentCategory

    override fun getCurrentQuiz() = mCurrentQuiz

    override fun getCurrentQuizList() = getQuizzes()

    override fun getStatsOption() = mResultsStats

    override fun saveStatsOption(resultsStats: Boolean) {
        mResultsStats = resultsStats
    }

    override fun getCurrentRound() = mCurrentRound

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
            {
                mGetQuizApiFailed = true
                Timber.d(it, "Error occurred while getting request for quiz!")
                val quizCacheEmpty = mContestantsCache.quizCache?.isEmpty() ?: true
                Timber.d("quizCacheEmpty: $quizCacheEmpty")
                val quizFromCache = mContestantsCache.currentQuizTitle
                Timber.d("current quiz from data source: $mCurrentQuiz, current quiz from cache: $quizFromCache")
                if (quizCacheEmpty || mCurrentQuiz != quizFromCache) {
                    mUnableToGetQuizJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                        mErrorMsgChannel.emit(mContext.getString(R.string.unable_to_get_quiz))
                    }
                }
            },
            lang = Locale.getDefault().language,
            public_super_category = mCurrentSuperCategory,
            public_category = mCurrentCategory,
            public_quiz = mCurrentQuiz
        )
    }

    private fun onGetQuizApiSuccess(response: Response<List<ContestantsInfo>>) {
        mGetQuizApiFailed = false
        response.body()?.let {
            val sortedQuizList: List<IContestantsInfo> = it.sortedBy { element -> element.name }
            Timber.d("onGetQuizApiSuccess(): current quiz=$mCurrentQuiz")
            if (mContestantsCache.isQuizCacheEmpty(mCurrentQuiz)) {
                mContestantsCache.updateQuizInfoCache(sortedQuizList, mCurrentQuiz)
                startQuiz(sortedQuizList)
            } else {
                mContestantsCache.quizCache?.let { quizCacheList ->
                    if (quizCacheList != sortedQuizList) {
                        Timber.d("quiz was updated from server")
                        mContestantsCache.updateQuizInfoCache(sortedQuizList, mCurrentQuiz)
                        getQuizList(mCurrentCategory)
                        mQuizUpdatedOnServerJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                            mErrorMsgChannel.emit(mContext.getString(R.string.quiz_updated_on_server))
                        }
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
                mRenderUiChannel.emit(getQuizItemDetailState())
            } catch (e: Exception) {
                Timber.e("Exception caught: $e")
            }
        }
    }

    private fun getSuperCategoriesApi() {
        Timber.d("getSuperCategoriesApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.SUPER_CATEGORIES,
            { onGetSuperCategoriesApiSuccess(it as Response<List<SuperCategory>>) },
            {
                Timber.d(it, "Error occurred while getting response for superCategories!")
                val superCategoriesCacheEmpty = mContestantsCache.superCategoriesCache?.isEmpty() ?: true
                Timber.d("superCategoriesCacheEmpty: $superCategoriesCacheEmpty")
                if (superCategoriesCacheEmpty) {
                    mUnableToGetSuperCategoriesJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                        mRenderUiChannel.emit(RenderState.ErrorState(mContext.getString(R.string.unable_to_get_super_categories)))
                    }
                }
            },
            lang = Locale.getDefault().language
        )
    }

    private fun getCategoriesApi() {
        Timber.d("getCategoriesApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.CATEGORIES,
            { onGetCategoriesApiSuccess(it as Response<List<Category>>) },
            {
                Timber.d(it, "Error occurred while getting response for superCategories!")
                val categoriesCacheEmpty = mContestantsCache.categoriesCache?.isEmpty() ?: true
                Timber.d("categoriesCacheEmpty: $categoriesCacheEmpty")
                val superCategoryFromCache = mContestantsCache.currentSuperCategoryName
                Timber.d("current superCategory from data source: $mCurrentSuperCategory, current superCategory from cache: $superCategoryFromCache")
                if (categoriesCacheEmpty || mCurrentSuperCategory != superCategoryFromCache) {
                    mUnableToGetCategoriesJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                        mErrorMsgChannel.emit(mContext.getString(R.string.unable_to_get_categories))
                    }
                }
            },
            lang = Locale.getDefault().language,
            public_super_category = mCurrentSuperCategory
        )
    }

    private fun getQuizzesApi() {
        Timber.d("getQuizzesApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.QUIZZES,
            { onGetQuizzesApiSuccess(it as Response<List<QuizShortInfo>>) },
            { onGetQuizzesApiError(it) },
            lang = Locale.getDefault().language,
            public_super_category = mCurrentSuperCategory,
            public_category = mCurrentCategory
        )
    }

    private fun getAllQuizzesApi() {
        Timber.d("getAllQuizzesApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.ALL_QUIZZES,
            { onGetQuizzesApiSuccess(it as Response<List<QuizShortInfo>>) },
            { onGetQuizzesApiError(it) },
            lang = Locale.getDefault().language
        )
    }

    private fun onGetSuperCategoriesApiSuccess(response: Response<List<SuperCategory>>) {
        response.body()?.let {
            Timber.d("onGetSuperCategoriesApiSuccess(): superCategories size: ${it.size}")
            val sortedSuperCategories = it.sortedBy { element -> element.name }
            val mutableSortedSuperCategories = sortedSuperCategories.toMutableList()
            // add fake 'All' super category
            mutableSortedSuperCategories.add(0,
                SuperCategory(
                    mContext.getString(R.string.all),
                    EMPTY_STRING,
                    EMPTY_STRING
                )
            )
            val listsUnequal = mContestantsCache.superCategoriesCache != mutableSortedSuperCategories
            Timber.d("listsUnequal: $listsUnequal")
            if (listsUnequal) {
                Timber.d("onGetSuperCategoriesApiSuccess(): got new superCategories from server")
                mContestantsCache.updateSuperCategoriesCache(mutableSortedSuperCategories)
                mSuperCategoriesUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mRenderUiChannel.emit(RenderState.SuperCategoriesState(mutableSortedSuperCategories))
                }
            }
        }
    }

    private fun onGetCategoriesApiSuccess(response: Response<List<Category>>) {
        response.body()?.let {
            val sortedCategories: List<ICategory> = it.sortedBy { element -> element.name }
            if (mContestantsCache.isCategoriesCacheEmpty(mCurrentSuperCategory)) {
                mContestantsCache.updateCategoriesCache(sortedCategories, mCurrentSuperCategory)
                mCategoriesUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mRenderUiChannel.emit(RenderState.CategoriesState(sortedCategories))
                }
            } else {
                mContestantsCache.categoriesCache?.let { categories ->
                    if (categories != sortedCategories) {
                        Timber.d("categories were updated from server")
                        mContestantsCache.updateCategoriesCache(sortedCategories, mCurrentSuperCategory)
                        getCategories(mCurrentSuperCategory)
                    }
                }
            }
        }
    }

    private fun onGetQuizzesApiSuccess(response: Response<List<QuizShortInfo>>) {
        response.body()?.let {
            Timber.d("onGetQuizzesApiSuccess(): quizzes size: ${it.size}")
            val sortedQuizShortInfoList = it.sortedBy { element -> element.title }
            mCurrentQuizzesList = sortedQuizShortInfoList
            if (mContestantsCache.isQuizzesCacheEmpty(mCurrentCategory)) {
                mContestantsCache.updateQuizzesInfoCache(sortedQuizShortInfoList, mCurrentCategory)
                mQuizListUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mRenderUiChannel.emit(RenderState.QuizListState(sortedQuizShortInfoList))
                }
            } else {
                mContestantsCache.quizzesInfoCache?.let { quizzes ->
                    if (quizzes != sortedQuizShortInfoList) {
                        Timber.d("quizzes were updated from server")
                        mContestantsCache.updateQuizzesInfoCache(sortedQuizShortInfoList, mCurrentCategory)
                        getQuizList(mCurrentCategory)
                    }
                }
            }
        }
    }

    private fun onGetQuizzesApiError(throwable: Throwable) {
        Timber.d(throwable, "Error occurred while getting response for quizzes list!")
        val quizzesCacheEmpty = mContestantsCache.quizzesInfoCache?.isEmpty() ?: true
        Timber.d("quizzesCacheEmpty: $quizzesCacheEmpty")
        val categoryFromCache = mContestantsCache.currentCategoryName
        Timber.d("current category from data source: $mCurrentCategory, current category from cache: $categoryFromCache")
        if (quizzesCacheEmpty || mCurrentCategory != categoryFromCache) {
            mUnableToGetQuizListJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mErrorMsgChannel.emit(mContext.getString(R.string.unable_to_get_quiz_list))
            }
        }
    }

    private fun getQuizStatsApi() {
        Timber.d("getQuizStatsApi()")
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.QUIZ_STATS,
            { onGetQuizStatsApiSuccess(it as Response<List<ContestantsStatsInfo>>) },
            {
                Timber.d(it, "Error occurred while getting request for contestants stats!")
                mUnableToGetStatsJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                    mErrorMsgChannel.emit(mContext.getString(R.string.unable_to_get_stats))
                }
            },
            lang = Locale.getDefault().language,
            public_super_category = mCurrentSuperCategory,
            public_category = mCurrentCategory,
            public_quiz = mCurrentQuiz
        )
    }

    private fun onGetQuizStatsApiSuccess(response: Response<List<ContestantsStatsInfo>>) {
        response.body()?.let {
            Timber.d("onGetQuizStatsApiSuccess(): quiz stats size: ${it.size}")
            mQuizStatsUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mTop4Array.forEach { elem ->
                    if (elem == null) {
                        Timber.w("Some of array elements are null")
                        return@launch
                    }
                }
                (mTop4Array.toList() as? List<IContestantsInfo>)?.let { list ->
                    mRenderUiChannel.emit(RenderState.StatsListState(it.sortedByDescending { element -> element.percentage.toFloat() }, list))
                }
                mTop4Array = arrayOfNulls(MAX_TOP_4_ITEMS)
            }
        }
    }

    private fun postWinner(winner: String) {
        mRestApiHelper.makeRequest(
            RestApiHelper.RequestType.WINNER,
            { onPostWinnerSuccess(it as Response<String>) },
            {
                Timber.d(it, "Error occurred while getting response for sending winner!")
                if (mDeferredPost) {
                    Timber.d("Some of the deferred requests failed")
                    mDeferredPost = false
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        mUnableToPostWinnerJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                            mErrorMsgChannel.emit(mContext.getString(R.string.unable_to_post_winner))
                            val winnerItem = if (winner.contains(POST_WINNER_SEPARATOR)) winner else "$winner$POST_WINNER_SEPARATOR$mCurrentQuiz"
                            if (!mDeferredWinnersList.contains(winnerItem)) {
                                Timber.d("adding winnerItem: $winnerItem")
                                mDeferredWinnersList.add(winnerItem)
                            }
                        }
                    },
                        POST_WINNER_ERROR_TIMEOUT
                    )
                }
            },
            lang = Locale.getDefault().language,
            public_super_category = mCurrentSuperCategory,
            public_category = mCurrentCategory,
            public_quiz = mCurrentQuiz,
            bodyData = if (mDeferredPost) winner else "$winner;$mCurrentQuiz"
        )
    }

    private fun onPostWinnerSuccess(response: Response<String>) {
        if (mDeferredPost) {
            Timber.d("mDeferredWinnersList size: ${mDeferredWinnersList.size}")
            if (mDeferredWinnersList.isNotEmpty()) {
                mDeferredWinnersList.removeAt(0)
            }
            if (mDeferredWinnersList.isEmpty()) {
                Timber.d("all deferred responses are successful")
                mDeferredPost = false
            }
        } else {
            response.body()?.let {
                if (it == WINNER_RESPONSE) {
                    mPostWinnerSuccessUpdateUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                        mLastWinnerState?.let { lastWinnerState ->
                            Timber.d("onPostWinnerSuccess(): ${lastWinnerState.winner}")
                            mRenderUiChannel.emit(RenderState.WinnerFinalState(lastWinnerState.winner))
                        }
                    }
                }
            }
        }
    }

    private fun getQuizItemDetailState(sameQuiz: Boolean = false): RenderState.QuizItemDetailState {
        if (!sameQuiz) {
            mFirstContestant = mInterimContestants.random()
            mInterimContestants.remove(mFirstContestant)
            mSecondContestant = mInterimContestants.random()
            mInterimContestants.remove(mSecondContestant)
        }
        Timber.d("getQuizItemDetailState(): current quiz: $mCurrentQuiz, mCurrentQuizzesList size: ${mCurrentQuizzesList.size}")
        if (mCurrentQuizzesList.isEmpty()) {
            mContestantsCache.quizzesInfoCache?.let { mCurrentQuizzesList = it }
        }
        val filteredCurrentQuizzesList = mCurrentQuizzesList.filter { it.title == mCurrentQuiz }
        Timber.d("filteredCurrentQuizzesList size: ${filteredCurrentQuizzesList.size}")
        val description = if (filteredCurrentQuizzesList.isNotEmpty()) filteredCurrentQuizzesList[0].description else mContext.getString(R.string.default_quiz_description)
        return RenderState.QuizItemDetailState(
            mFirstContestant, mSecondContestant, if (mCurrentRound == 1) mContext.getString(R.string.final_round) else "1/$mCurrentRound", mCurrentQuiz, description)
    }
}