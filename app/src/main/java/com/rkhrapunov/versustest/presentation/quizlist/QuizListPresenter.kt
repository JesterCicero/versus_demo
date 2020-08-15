package com.rkhrapunov.versustest.presentation.quizlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.rkhrapunov.core.data.IContestantsStatsInfo
import com.rkhrapunov.core.data.IQuizShortInfo
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.domain.RenderState
import com.rkhrapunov.core.interactors.GetQuizItemDetailInteractor
import com.rkhrapunov.core.interactors.GetQuizListInteractor
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.base.BasePresenter
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

@ExperimentalStdlibApi
@FlowPreview
@ExperimentalCoroutinesApi
class QuizListPresenter : BasePresenter<IQuizListContract.IQuizListView>(),
    IQuizListContract.IQuizListPresenter, KoinComponent, IItemClickListener {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mGetQuizListInteractor by inject<GetQuizListInteractor>()
    private val mGetQuizItemDetailInteractor by inject<GetQuizItemDetailInteractor>()
    private var mJob: Job? = null
    private var mAllContestants = emptyList<IQuizShortInfo>()
    private var mAllContestantsStats = emptyList<IContestantsStatsInfo>()

    override fun attachView(view: IQuizListContract.IQuizListView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .filter { it is RenderState.StatsListState && mAllContestantsStats != it.statsContestants }
                .collect { onCollectRenderState(it) }
        }
        val shouldShowQuizList = mView?.shouldShowQuizList() ?: false
        Timber.d("attachView(): shouldShowQuizList=$shouldShowQuizList")
        if (shouldShowQuizList) {
            mGetQuizListInteractor.getQuizList()
        }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }

    override fun onBackToQuizzesButtonClickedIntent() {
        mView?.onBackToQuizzesButtonClicked()
    }

    override fun onItemClickedIntent(itemData: String) = mGetQuizItemDetailInteractor.getQuizItemDetail(itemData)

    private fun onCollectRenderState(renderState: IRenderState) {
        Timber.d("attachView(): renderState=$renderState")
        if (renderState is RenderState.QuizListState) {
            mAllContestants = renderState.allContestants
            mView?.let {
                val adapter = QuizListAdapter<IQuizShortInfo>(this@QuizListPresenter, it as Fragment)
                it.setAdapter(adapter)
                adapter.updateData(mAllContestants)
            }
        } else if (renderState is RenderState.StatsListState) {
            mAllContestantsStats = renderState.statsContestants
            mView?.let {
                val adapter = QuizListAdapter<IContestantsStatsInfo>(this@QuizListPresenter, it as Fragment, false)
                it.setAdapter(adapter)
                adapter.updateData(mAllContestantsStats)
            }
        }
    }
}