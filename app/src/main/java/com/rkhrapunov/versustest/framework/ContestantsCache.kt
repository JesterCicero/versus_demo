package com.rkhrapunov.versustest.framework

import com.rkhrapunov.core.data.IContestantsInfo
import com.rkhrapunov.core.data.IQuizShortInfo
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.Constants.EMPTY_STRING
import com.rkhrapunov.versustest.presentation.base.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

@ExperimentalCoroutinesApi
class ContestantsCache : KoinComponent {

    var quizzesInfoCache: List<IQuizShortInfo>? = null
        private set

    var quizCache: List<IContestantsInfo>? = null
        private set

    private var mCurrentQuizTitle = EMPTY_STRING

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannel by inject<ConflatedBroadcastChannel<IRenderState>>(named("RenderState"))
    private val mPreferences by inject<Preferences>()

    fun tryToGetQuizzesInfoCache() {
        Timber.d("tryToGetQuizzesInfoCache()")
        quizzesInfoCache?.let {
            Timber.d("tryToGetQuizzesInfoCache(): cache from memory")
            sendQuizzesList(it)
        } ?: run {
            mPreferences.quizzes?.let {
                if (it.isEmpty()) { return@let null }
                val quizShortInfoList = it.toList().map { element ->
                    val list = element.split(DELIMITER)
                    QuizShortInfo(list[0], list[1])
                }.sortedBy { element -> element.title }
                if (quizShortInfoList.isEmpty()) { return@let null }
                Timber.d("tryToGetQuizzesInfoCache(): cache from preferences")
                quizzesInfoCache = quizShortInfoList
                sendQuizzesList(quizShortInfoList)
            }
        }
    }

    fun updateQuizzesInfoCache(updatedQuizzesInfoCache: List<IQuizShortInfo>) {
        Timber.d("updateQuizzesInfoCache()")
        quizzesInfoCache = updatedQuizzesInfoCache
        quizCache = null
        mPreferences.quizzes?.forEach { mPreferences.removeQuiz(it.substring(0, it.indexOf(DELIMITER))) }
        mPreferences.quizzes = updatedQuizzesInfoCache.map { "${it.title}$DELIMITER${it.url}" }.toSet()
    }

    fun tryToGetQuizInfoCache(quizTitle: String): List<IContestantsInfo>? {
        Timber.d("tryToGetQuizInfoCache()")
        return quizCache?.let {
            Timber.d("currentQuizTitle: $mCurrentQuizTitle, quizTitle: $quizTitle")
            if (mCurrentQuizTitle.isNotEmpty() && mCurrentQuizTitle != quizTitle) { return@let null }
            Timber.d("tryToGetQuizInfoCache(): cache from memory")
            mCurrentQuizTitle = quizTitle
            it
        } ?: run {
            mPreferences.getQuiz(quizTitle)?.let {
                if (it.isEmpty()) { return@let null }
                val contestantsList = it.toList().map { element ->
                    val list = element.split(DELIMITER)
                    ContestantsInfo(list[0], list[1])
                }.sortedBy { element -> element.name }
                if (contestantsList.isEmpty()) { return@let null }
                Timber.d("tryToGetQuizInfoCache(): cache from preferences")
                quizCache = contestantsList
                mCurrentQuizTitle = quizTitle
                contestantsList
            }
        }
    }

    fun updateQuizInfoCache(updatedQuizInfoCache: List<IContestantsInfo>, currentQuiz: String) {
        Timber.d("updateQuizInfoCache()")
        mCurrentQuizTitle = currentQuiz
        quizCache = updatedQuizInfoCache
        mPreferences.saveQuiz(mCurrentQuizTitle, updatedQuizInfoCache.map { "${it.name};${it.url}" }.toSet())
    }

    fun isQuizCacheEmpty(currentQuiz: String) = mPreferences.getQuiz(currentQuiz)?.isEmpty() ?: true

    private fun sendQuizzesList(quizzesList: List<IQuizShortInfo>) {
        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannel.send(RenderState.QuizListState(quizzesList))
        }
    }

    companion object {
        private const val DELIMITER = ";"
    }
}