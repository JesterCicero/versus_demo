package com.avp.ctbo.presentation.empty_pager

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.GetQuizListInteractor
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.GetSuperCategoriesInteractor
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.BasePresenter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@DelicateCoroutinesApi
class EmptyPagerPresenter : BasePresenter<IEmptyPagerContract.IEmptyPagerView>(),
    IEmptyPagerContract.IEmptyPagerPresenter, KoinComponent {

    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()
    private val mGetSuperCategoriesInteractor by inject<GetSuperCategoriesInteractor>()
    @Suppress("unused")
    private val mGetQuizListInteractor by inject<GetQuizListInteractor>()
    private val mRenderUiChannelInteractor by inject<GetRenderUiChannelInteractor>()
    private var mJob: Job? = null
    private var mCurrentState: IRenderState? = null

    override fun attachView(view: IEmptyPagerContract.IEmptyPagerView, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        super.attachView(view, viewLifecycle, savedInstanceState)
        mJob = mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mRenderUiChannelInteractor.getRenderUiChannel()
                .filter { it is RenderState.ErrorState }
                .collect { renderState ->
                    Timber.d("attachView(): renderState=$renderState")
                    if (renderState is RenderState.ErrorState) {
                        mCurrentState?.let {
                            mView?.showToast()
                        } ?: mView?.renderErrorState(renderState)
                        mCurrentState = renderState
                    }
                }
        }
    }

    override fun onRetryLoadingClickedIntent() = mGetSuperCategoriesInteractor.getSuperCategories()

    override fun onViewDestroyed() {
        mJob?.cancel()
        super.onViewDestroyed()
    }
}