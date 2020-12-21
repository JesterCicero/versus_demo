package com.avp.ctbo.presentation.quiz_pager

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.PageIndicatorTextInteractor
import com.avp.core.interactors.GetCategoriesInteractor
import com.avp.core.interactors.GetCurrentSuperCategoryInteractor
import com.avp.core.interactors.GetCurrentCategoryInteractor
import com.avp.ctbo.framework.ContestantsCache
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.BasePresenter
import com.avp.ctbo.presentation.base.Constants.EMPTY_STRING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class QuizPagerPresenter : BasePresenter<IQuizPagerContract.IQuizPagerView>(),
    IQuizPagerContract.IQuizPagerPresenter, KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mPageIndicatorTextInteractor by inject<PageIndicatorTextInteractor>()
    private val mGetCategoriesInteractor by inject<GetCategoriesInteractor>()
    private val mGetCurrentSuperCategoryInteractor by inject<GetCurrentSuperCategoryInteractor>()
    private val mGetCurrentCategoryInteractor by inject<GetCurrentCategoryInteractor>()
    private val mContestantsCache by inject<ContestantsCache>()
    private var mJob: Job? = null
    private var mCurrentState: IRenderState? = null

    override fun attachView(view: IQuizPagerContract.IQuizPagerView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        if (savedInstanceState == null) {
            mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mRenderUiChannelInteractor.getRenderUiChannel()
                    .asFlow()
                    .filter { it is RenderState.SuperCategoriesState
                            || it is RenderState.CategoriesState
                            || it is RenderState.QuizListState }
                    .collect {
                        mCurrentState = it
                        mView?.renderState(it)
                    }
            }
        }
    }

    override fun savePageIndicatorText(pageIndicatorText: String) = mPageIndicatorTextInteractor.savePageIndicatorText(pageIndicatorText)

    override fun getPageIndicatorText() = mPageIndicatorTextInteractor.getPageIndicatorText()

    override fun saveCurrentPagePosition(currentPosition: Int) = mPageIndicatorTextInteractor.saveCurrentPagePosition(currentPosition)

    override fun getCurrentPagePosition() = mPageIndicatorTextInteractor.getCurrentPagePosition()

    override fun getCurrentSuperCategoryBackgroundUrl(): String {
        val filteredSuperCategories = mContestantsCache.superCategoriesCache?.filter {
            it.name == mGetCurrentSuperCategoryInteractor.getCurrentSuperCategory()
        }
        return filteredSuperCategories?.let {
            if (it.isNotEmpty()) it[0].backgroundUrl else EMPTY_STRING
        } ?: EMPTY_STRING
    }

    override fun getCurrentCategoryBackgroundUrl(): String {
        val filteredCategories = mContestantsCache.categoriesCache?.filter {
            it.name == mGetCurrentCategoryInteractor.getCurrentCategory()
        }
        return filteredCategories?.let {
            if (it.isNotEmpty()) it[0].backgroundUrl else EMPTY_STRING
        } ?: EMPTY_STRING
    }

    override fun getCurrentSuperCategory() = mGetCurrentSuperCategoryInteractor.getCurrentSuperCategory()

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }

    override fun onBackPressed() {
        Timber.d("onBackPressed(): current state: $mCurrentState")
        mCurrentState?.let {
            when (it) {
                is RenderState.CategoriesState -> mView?.onSuperCategoriesBack()
                is RenderState.QuizListState -> mGetCategoriesInteractor.getCategories()
                else -> mView?.onSuperCategoriesBack()
            }
        }
    }
}