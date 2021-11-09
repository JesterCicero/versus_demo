package com.avp.ctbo.presentation.main

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.GetErrorMsgChannelInteractor
import com.avp.core.interactors.GetCurrentQuizListInteractor
import com.avp.core.interactors.GetSuperCategoriesInteractor
import com.avp.core.interactors.GetCategoriesInteractor
import com.avp.core.interactors.CancelQuizInteractor
import com.avp.core.interactors.CurrentSuperCategoryInteractor
import com.avp.core.interactors.GetStatsOptionInteractor
import com.avp.ctbo.framework.ContestantsCache
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.BasePresenter
import com.avp.ctbo.presentation.base.Constants.INVALID_VALUE
import com.avp.ctbo.presentation.base.IItemClickListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@DelicateCoroutinesApi
class MainPresenter : BasePresenter<IMainContract.IMainView>(), IMainContract.IMainPresenter,
    IItemClickListener, KoinComponent {

    private val mContestantsCache by inject<ContestantsCache>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mErrorMsgChannelInteractor by inject<GetErrorMsgChannelInteractor>()
    private val mGetCurrentQuizListInteractor by inject<GetCurrentQuizListInteractor>()
    private val mGetSuperCategoriesInteractor by inject<GetSuperCategoriesInteractor>()
    private val mGetCategoriesInteractor by inject<GetCategoriesInteractor>()
    private val mCancelQuizInteractor by inject<CancelQuizInteractor>()
    private val mCurrentSuperCategoryInteractor by inject<CurrentSuperCategoryInteractor>()
    private val mGetStatsOptionInteractor by inject<GetStatsOptionInteractor>()
    private var mRenderUiJob: Job? = null
    private var mErrorMsgJob: Job? = null
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private var mCurrentState: IRenderState? = null

    override fun attachView(view: IMainContract.IMainView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        subscribeRenderUiChannel()
        subscribeErrorMsgChannel()
    }

    override fun updateSuperCategoryPosition(newPosition: Int) = mCurrentSuperCategoryInteractor.saveCurrentSuperCategoryPosition(newPosition)

    override fun updateSuperCategoryOnError() {
        mView?.onSuperCategoryChanged(mCurrentSuperCategoryInteractor.getCurrentSuperCategoryPosition(),
            mCurrentSuperCategoryInteractor.getPreviousSuperCategoryPosition())
    }

    override fun onInitialSuperCategory() {
        Timber.d("onInitialSuperCategory()")
        val currentSuperCategoryPosition = mCurrentSuperCategoryInteractor.getCurrentSuperCategoryPosition()
        val position = if (currentSuperCategoryPosition == INVALID_VALUE) 0 else currentSuperCategoryPosition
        Timber.d("onInitialSuperCategory(): currentState: $mCurrentState")
        mCurrentState?.let {
            if (it is RenderState.SuperCategoriesState) {
                onItemClickedIntent(it.superCategories[position].name, position)
            }
        }
    }

    override fun getSuperCategories() = mGetSuperCategoriesInteractor.getSuperCategories()

    override fun getSuperCategoriesFromCache() = mContestantsCache.superCategoriesCache

    override fun resetResultsStatsOption() = mGetStatsOptionInteractor.saveStatsOption(true)

    override fun getCurrentSuperCategoryPosition() = mCurrentSuperCategoryInteractor.getCurrentSuperCategoryPosition()

    override fun cancelQuiz() {
        mCancelQuizInteractor.cancelQuiz()
        mGetCurrentQuizListInteractor.getCurrentQuizList()
    }

    override fun onViewDestroyed() {
        mRenderUiJob?.cancel()
        mErrorMsgJob?.cancel()
        super.onViewDestroyed()
    }

    override fun onItemClickedIntent(itemData: String, position: Int) {
        val currentSuperCategoryPosition = mCurrentSuperCategoryInteractor.getCurrentSuperCategoryPosition()
        Timber.d("onItemClickedIntent(): current position: $currentSuperCategoryPosition, new position: $position")
        mGetCategoriesInteractor.getCategories(itemData)
        mView?.onSuperCategoryChanged(currentSuperCategoryPosition, position)
    }

    private fun subscribeRenderUiChannel() {
        mRenderUiJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .filter { it != mCurrentState }
                .collect {
                    Timber.d("$mCurrentState -> $it")
                    mCurrentState = it
                    mView?.render(it)
                }
        }
    }

    private fun subscribeErrorMsgChannel() {
        mErrorMsgJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mErrorMsgChannelInteractor.getErrorMsgChannel()
                .collect {
                    Timber.d("Error message: $it")
                    mView?.renderErrorState(it)
                }
        }
    }
}