package com.rkhrapunov.versustest.framework.di

import com.rkhrapunov.core.data.ContestantsRepository
import com.rkhrapunov.core.data.IContestantsDataSource
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.interactors.ChosenContestantInteractor
import com.rkhrapunov.core.interactors.GetRenderUiChannelInteractor
import com.rkhrapunov.core.interactors.ResetInteractor
import com.rkhrapunov.versustest.framework.ContestantsDataSource
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.presentation.main.IMainContract
import com.rkhrapunov.versustest.presentation.main.MainPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
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
    factory<IContestantsDataSource> { ContestantsDataSource() }
    factory { GetRenderUiChannelInteractor() }
    factory { ChosenContestantInteractor() }
    factory { ResetInteractor() }
    factory<IMainContract.IMainPresenter> { MainPresenter() }
    single(named("RenderState")) { ConflatedBroadcastChannel<IRenderState>() }
}