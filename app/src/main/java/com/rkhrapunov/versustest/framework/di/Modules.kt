package com.rkhrapunov.versustest.framework.di

import com.rkhrapunov.core.data.ContestantsRepository
import com.rkhrapunov.core.data.IContestantsDataSource
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.core.interactors.ChosenContestantInteractor
import com.rkhrapunov.core.interactors.GetQuizListInteractor
import com.rkhrapunov.core.interactors.GetQuizItemDetailInteractor
import com.rkhrapunov.core.interactors.ResetInteractor
import com.rkhrapunov.core.interactors.GetStatsInteractor
import com.rkhrapunov.versustest.framework.ContestantsDataSource
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.framework.helpers.RestApiHelper
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.main.IMainContract
import com.rkhrapunov.versustest.presentation.main.MainPresenter
import com.rkhrapunov.versustest.presentation.quiz_detail.IQuizItemDetailContract
import com.rkhrapunov.versustest.presentation.quiz_detail.QuizItemDetailPresenter
import com.rkhrapunov.versustest.presentation.quizlist.IQuizListContract
import com.rkhrapunov.versustest.presentation.quizlist.QuizListPresenter
import com.rkhrapunov.versustest.presentation.winner.IWinnerContract
import com.rkhrapunov.versustest.presentation.winner.WinnerPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

@FlowPreview
@ExperimentalCoroutinesApi
val applicationModule = module(override = true) {
    single { androidApplication() }
    single { ContestantsRepository() }
    single { CoroutineLauncherHelper() }
    single { RestApiHelper() }
    single { ImageLoader() }
    single(named("RenderState")) { ConflatedBroadcastChannel<IRenderState>() }
    factory<IContestantsDataSource> { ContestantsDataSource() }
    factory { GetRenderUiChannelInteractor() }
    factory { ChosenContestantInteractor() }
    factory { GetQuizListInteractor() }
    factory { GetQuizItemDetailInteractor() }
    factory { ResetInteractor() }
    factory { GetStatsInteractor() }
    factory<IMainContract.IMainPresenter> { MainPresenter() }
    factory<IQuizListContract.IQuizListPresenter> { QuizListPresenter() }
    factory<IQuizItemDetailContract.IQuizItemDetailPresenter> { QuizItemDetailPresenter() }
    factory<IWinnerContract.IWinnerPresenter> { WinnerPresenter() }
}