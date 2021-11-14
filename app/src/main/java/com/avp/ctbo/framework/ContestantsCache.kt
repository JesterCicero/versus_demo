package com.avp.ctbo.framework

import com.avp.core.data.ICategory
import com.avp.core.data.IContestantsInfo
import com.avp.core.data.IQuizShortInfo
import com.avp.core.data.ISuperCategory
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.Constants.EMPTY_STRING
import com.avp.ctbo.presentation.base.Preferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber

@DelicateCoroutinesApi
class ContestantsCache : KoinComponent {

    var superCategoriesCache: List<ISuperCategory>? = null
        private set

    var categoriesCache: List<ICategory>? = null
        private set

    var quizzesInfoCache: List<IQuizShortInfo>? = null
        private set

    var quizCache: List<IContestantsInfo>? = null
        private set

    val currentQuizTitle: String
        get() = mCurrentQuizTitle

    val currentSuperCategoryName: String
        get() = mCurrentSuperCategoryName

    val currentCategoryName: String
        get() = mCurrentCategoryName

    private var mCurrentQuizTitle = EMPTY_STRING
    private var mCurrentSuperCategoryName = EMPTY_STRING
    private var mCurrentCategoryName = EMPTY_STRING

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannel by inject<MutableStateFlow<IRenderState>>(named("RenderState"))
    private val mPreferences by inject<Preferences>()

    fun tryToGetSuperCategoriesCache() {
        Timber.d("tryToGetSuperCategoriesCache()")
        superCategoriesCache?.let {
            Timber.d("tryToGetSuperCategoriesCache(): cache from memory")
            sendSuperCategories(it)
        } ?: run {
            mPreferences.superCategories?.let {
                if (it.isEmpty()) { return@let null }
                val superCategories = it.toList().map { element ->
                    val list = element.split(DELIMITER)
                    SuperCategory(
                        list[0],
                        list[1],
                        list[2]
                    )
                }.sortedBy { element -> element.name }
                if (superCategories.isEmpty()) { return@let null }
                Timber.d("tryToGetQuizzesInfoCache(): cache from preferences")
                superCategoriesCache = superCategories
                sendSuperCategories(superCategories)
            }
        }
    }

    fun tryToGetCategoriesCache(superCategoryName: String) {
        Timber.d("tryToGetCategoriesCache()")
        categoriesCache?.let {
            Timber.d("current superCategory: $mCurrentSuperCategoryName, superCategory: $superCategoryName")
            if (mCurrentSuperCategoryName.isNotEmpty() && mCurrentSuperCategoryName != superCategoryName) { return@let null }
            Timber.d("tryToGetCategoriesCache(): cache from memory")
            setCurrentSuperCategoryAndSendCategories(superCategoryName, it)
        } ?: run {
            mPreferences.getSuperCategory(superCategoryName)?.let {
                if (it.isEmpty()) { return@let null }
                val categories = it.toList().map { element ->
                    val list = element.split(DELIMITER)
                    Category(
                        list[0],
                        list[1],
                        list[2]
                    )
                }.sortedBy { element -> element.name }
                if (categories.isEmpty()) { return@let null }
                Timber.d("tryToGetCategoriesCache(): cache from preferences")
                categoriesCache = categories
                setCurrentSuperCategoryAndSendCategories(superCategoryName, categories)
            }
        }
    }

    fun tryToGetQuizzesInfoCache(categoryName: String): Boolean {
        Timber.d("tryToGetQuizzesInfoCache()")
        return quizzesInfoCache?.let {
            Timber.d("current category: $mCurrentCategoryName, category: $categoryName")
            if (mCurrentCategoryName.isNotEmpty() && mCurrentCategoryName != categoryName) { return@let null }
            Timber.d("tryToGetQuizzesInfoCache(): cache from memory")
            setCurrentCategoryAndSendQuizzes(categoryName, it)
            true
        } ?: run {
            mPreferences.getCategory(categoryName)?.let {
                if (it.isEmpty()) { return@let null }
                val quizShortInfoList = it.toList().map { element ->
                    val list = element.split(DELIMITER)
                    if (list.size < QUIZ_CACHE_SHORT_INFO_LIST_NUMBER) { return@let null }
                    Timber.d("tryToGetQuizzesInfoCache(): list[0]=${list[0]}, list[1]=${list[1]}, list[2]=${list[2]}, list[3]=${list[3]}")
                    QuizShortInfo(
                        list[0],
                        list[1],
                        list[2],
                        list[3]
                    )
                }.sortedBy { element -> element.title }
                if (quizShortInfoList.isEmpty()) { return@let null }
                Timber.d("tryToGetQuizzesInfoCache(): cache from preferences")
                quizzesInfoCache = quizShortInfoList
                setCurrentCategoryAndSendQuizzes(categoryName, quizShortInfoList)
                true
            } ?: false
        }
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
                    ContestantsInfo(
                        list[0],
                        list[1],
                        list[2],
                        list[3]
                    )
                }.sortedBy { element -> element.name }
                if (contestantsList.isEmpty()) { return@let null }
                Timber.d("tryToGetQuizInfoCache(): cache from preferences")
                quizCache = contestantsList
                mCurrentQuizTitle = quizTitle
                contestantsList
            }
        }
    }

    private fun setCurrentSuperCategoryAndSendCategories(superCategoryName: String, categories: List<ICategory>) {
        mCurrentSuperCategoryName = superCategoryName
        sendCategories(categories)
    }

    private fun setCurrentCategoryAndSendQuizzes(categoryName: String, quizzes: List<IQuizShortInfo>) {
        mCurrentCategoryName = categoryName
        sendQuizzesList(quizzes)
    }

    fun updateSuperCategoriesCache(updatedSuperCategoriesCache: List<ISuperCategory>) {
        Timber.d("updateSuperCategoriesCache()")
        superCategoriesCache = updatedSuperCategoriesCache
        categoriesCache = null
        quizzesInfoCache = null
        quizCache = null
        clearAllPreferencesCache()
        mPreferences.superCategories = updatedSuperCategoriesCache.map { "${it.name}$DELIMITER${it.url}$DELIMITER${it.backgroundUrl}" }.toSet()
    }

    fun updateCategoriesCache(updatedCategoriesCache: List<ICategory>, currentSuperCategory: String) {
        Timber.d("updateCategoriesCache()")
        mCurrentSuperCategoryName = currentSuperCategory
        categoriesCache = updatedCategoriesCache
        quizzesInfoCache = null
        quizCache = null
        clearCategoriesPreferencesCache()
        mPreferences.saveSuperCategory(mCurrentSuperCategoryName, updatedCategoriesCache.map { "${it.name}$DELIMITER${it.url}$DELIMITER${it.backgroundUrl}" }.toSet())
    }

    fun updateQuizzesInfoCache(updatedQuizzesInfoCache: List<IQuizShortInfo>, currentCategory: String) {
        Timber.d("updateQuizzesInfoCache()")
        mCurrentCategoryName = currentCategory
        quizzesInfoCache = updatedQuizzesInfoCache
        quizCache = null
        clearQuizzesPreferencesCache()
        mPreferences.saveCategory(mCurrentCategoryName, updatedQuizzesInfoCache.map {
            Timber.d("updateQuizzesInfoCache(): ${it.title}$DELIMITER${it.description}$DELIMITER${it.url}$DELIMITER${it.backgroundUrl}")
            "${it.title}$DELIMITER${it.description}$DELIMITER${it.url}$DELIMITER${it.backgroundUrl}"
        }.toSet())
    }

    fun updateQuizInfoCache(updatedQuizInfoCache: List<IContestantsInfo>, currentQuiz: String) {
        Timber.d("updateQuizInfoCache()")
        mCurrentQuizTitle = currentQuiz
        quizCache = updatedQuizInfoCache
        mPreferences.saveQuiz(mCurrentQuizTitle, updatedQuizInfoCache.map { "${it.name}$DELIMITER${it.url}$DELIMITER${it.minUrl}$DELIMITER${it.shortDescription}" }.toSet())
    }

    fun isCategoriesCacheEmpty(currentSuperCategory: String) = mPreferences.getSuperCategory(currentSuperCategory)?.isEmpty() ?: true
    fun isQuizzesCacheEmpty(currentCategory: String) =  mPreferences.getCategory(currentCategory)?.isEmpty() ?: true
    fun isQuizCacheEmpty(currentQuiz: String) = mPreferences.getQuiz(currentQuiz)?.isEmpty() ?: true

    private fun clearAllPreferencesCache() {
        mPreferences.superCategories?.forEach {
            val superCategory = it.substring(0, it.indexOf(DELIMITER))
            mPreferences.getSuperCategory(superCategory)?.forEach { categoryInfo ->
                mPreferences.getCategory(categoryInfo.substring(0, it.indexOf(DELIMITER)))?.forEach { quizInfo ->
                    mPreferences.removeQuiz(quizInfo)
                }
                mPreferences.removeCategory(categoryInfo)
            }
            mPreferences.removeSuperCategory(superCategory)
        }
    }

    private fun clearCategoriesPreferencesCache() {
        mPreferences.getSuperCategory(mCurrentSuperCategoryName)?.forEach {
            val category = it.substring(0, it.indexOf(DELIMITER))
            Timber.d("category: $category")
            mPreferences.getCategory(category)?.forEach { quizInfo ->
                mPreferences.removeQuiz(quizInfo)
            }
            mPreferences.removeCategory(category)
        }
    }

    private fun clearQuizzesPreferencesCache() {
        mPreferences.getCategory(mCurrentCategoryName)?.forEach {
            val quiz = it.substring(0, it.indexOf(DELIMITER))
            Timber.d("quiz: $quiz")
            mPreferences.removeQuiz(quiz)
        }
    }

    private fun sendSuperCategories(superCategories: List<ISuperCategory>) {
        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannel.emit(RenderState.SuperCategoriesState(superCategories))
        }
    }

    private fun sendCategories(categories: List<ICategory>) {
        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannel.emit(RenderState.CategoriesState(categories))
        }
    }

    private fun sendQuizzesList(quizzesList: List<IQuizShortInfo>) {
        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannel.emit(RenderState.QuizListState(quizzesList))
        }
    }

    companion object {
        private const val DELIMITER = ";"
        private const val QUIZ_CACHE_SHORT_INFO_LIST_NUMBER = 4
    }
}