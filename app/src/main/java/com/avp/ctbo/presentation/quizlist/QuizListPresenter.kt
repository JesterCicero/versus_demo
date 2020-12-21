package com.avp.ctbo.presentation.quizlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.avp.core.data.IContestantsInfo
import com.avp.core.data.IContestantsStatsInfo
import com.avp.core.data.IQuizShortInfo
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.GetCurrentQuizListInteractor
import com.avp.core.interactors.GetQuizItemDetailInteractor
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.GetStatsOptionInteractor
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.BasePresenter
import com.avp.ctbo.presentation.base.IItemClickListener
import com.avp.ctbo.presentation.base.QuizAdapter
import com.avp.ctbo.presentation.base.QuizDataType
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
    IQuizListContract.IQuizListPresenter, KoinComponent,
    IItemClickListener {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private val mGetCurrentQuizListInteractor by inject<GetCurrentQuizListInteractor>()
    private val mGetQuizItemDetailInteractor by inject<GetQuizItemDetailInteractor>()
    private val mGetStatsOptionInteractor by inject<GetStatsOptionInteractor>()
    private var mJob: Job? = null
    private var mAllContestants = emptyList<IQuizShortInfo>()
    private var mAllContestantsStats = emptyList<IContestantsStatsInfo>()
    private var mTop4List = emptyList<IContestantsInfo>()

    override fun attachView(view: IQuizListContract.IQuizListView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .asFlow()
                .filter { it is RenderState.StatsListState && mAllContestantsStats != it.statsContestants && mTop4List != it.top4List}
                .collect { onCollectRenderState(it) }
        }
        val shouldShowQuizList = mView?.shouldShowQuizList() ?: false
        Timber.d("attachView(): shouldShowQuizList=$shouldShowQuizList")
        if (shouldShowQuizList) {
            mGetCurrentQuizListInteractor.getCurrentQuizList()
        }
    }

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }

    override fun onStatsTabClicked(results: Boolean) {
        if (results) {
            updateResultsData()
        } else {
            updateTop4Data()
        }
        mView?.updateStatsHeaders(results)
        mGetStatsOptionInteractor.saveStatsOption(results)
    }

    override fun onBackToQuizzesButtonClickedIntent() {
        mView?.onBackToQuizzesButtonClicked()
    }

    override fun onItemClickedIntent(itemData: String, position: Int) = mGetQuizItemDetailInteractor.getQuizItemDetail(itemData)

    private fun onCollectRenderState(renderState: IRenderState) {
        Timber.d("attachView(): renderState=$renderState")
        if (renderState is RenderState.QuizListState) {
            mAllContestants = renderState.allContestants
            mView?.let {
                val adapter =
                    QuizAdapter<IQuizShortInfo>(
                        this@QuizListPresenter,
                        it as Fragment,
                        QuizDataType.QUIZ_LIST
                    )
                it.setAdapter(adapter)
                adapter.updateData(mAllContestants)
            }
        } else if (renderState is RenderState.StatsListState) {
            mAllContestantsStats = renderState.statsContestants
            mTop4List = renderState.top4List
            val resultsStats = mGetStatsOptionInteractor.getStatsOption()
            if (resultsStats) {
                updateResultsData()
            } else {
                updateTop4Data()
            }
            mView?.updateStatsHeaders(resultsStats)
        }
    }

    private fun updateResultsData() {
        mView?.let {
            val adapter =
                QuizAdapter<IContestantsStatsInfo>(
                    this@QuizListPresenter,
                    it as Fragment,
                    QuizDataType.QUIZ_STATS
                )
            it.setAdapter(adapter)
            adapter.updateData(mAllContestantsStats)
        }
    }

    private fun updateTop4Data() {
        mView?.let {
            val adapter =
                QuizAdapter<IContestantsInfo>(
                    this@QuizListPresenter,
                    it as Fragment,
                    QuizDataType.QUIZ_TOP_4
                )
            it.setAdapter(adapter)
            adapter.updateData(mTop4List)
            Timber.d("Not enough")
        }
    }
}