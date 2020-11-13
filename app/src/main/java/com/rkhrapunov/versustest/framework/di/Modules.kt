package com.rkhrapunov.versustest.framework.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.rkhrapunov.core.data.ContestantsRepository
import com.rkhrapunov.core.data.IContestantsDataSource
import com.rkhrapunov.core.domain.IRenderState
import com.rkhrapunov.core.interactors.*
import com.rkhrapunov.versustest.framework.ContestantsCache
import com.rkhrapunov.versustest.framework.ContestantsDataSource
import com.rkhrapunov.versustest.framework.helpers.CoroutineLauncherHelper
import com.rkhrapunov.versustest.framework.helpers.NetworkConnectivityHelper
import com.rkhrapunov.versustest.framework.helpers.RestApiHelper
import com.rkhrapunov.versustest.presentation.about.AboutDialogPresenter
import com.rkhrapunov.versustest.presentation.about.IAboutDialogContract
import com.rkhrapunov.versustest.presentation.base.ImageLoader
import com.rkhrapunov.versustest.presentation.base.Preferences
import com.rkhrapunov.versustest.presentation.empty_pager.EmptyPagerPresenter
import com.rkhrapunov.versustest.presentation.empty_pager.IEmptyPagerContract
import com.rkhrapunov.versustest.presentation.error.ErrorDialogPresenter
import com.rkhrapunov.versustest.presentation.error.IErrorDialogContract
import com.rkhrapunov.versustest.presentation.licenses.ILicensesDialogContract
import com.rkhrapunov.versustest.presentation.licenses.LicensesDialogPresenter
import com.rkhrapunov.versustest.presentation.main.IMainContract
import com.rkhrapunov.versustest.presentation.main.MainPresenter
import com.rkhrapunov.versustest.presentation.quiz_detail.IQuizItemDetailContract
import com.rkhrapunov.versustest.presentation.quiz_detail.QuizItemDetailPresenter
import com.rkhrapunov.versustest.presentation.quiz_page.IQuizPageContract
import com.rkhrapunov.versustest.presentation.quiz_page.QuizPagePresenter
import com.rkhrapunov.versustest.presentation.quiz_pager.IQuizPagerContract
import com.rkhrapunov.versustest.presentation.quiz_pager.QuizPagerPresenter
import com.rkhrapunov.versustest.presentation.quizlist.IQuizListContract
import com.rkhrapunov.versustest.presentation.quizlist.QuizListPresenter
import com.rkhrapunov.versustest.presentation.topsnackbar.ITopBarNotification
import com.rkhrapunov.versustest.presentation.topsnackbar.TopSnackBarHelper
import com.rkhrapunov.versustest.presentation.topsnackbar.TopSnackBarType
import com.rkhrapunov.versustest.presentation.winner.IWinnerContract
import com.rkhrapunov.versustest.presentation.winner.WinnerPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val PREFS_NAME = "prefs_name"

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@ExperimentalStdlibApi
@FlowPreview
@ExperimentalCoroutinesApi
val applicationModule = module(override = true) {
    single { androidApplication() }
    single { ContestantsRepository() }
    single { CoroutineLauncherHelper() }
    single { RestApiHelper() }
    single { ImageLoader() }
    single { Preferences() }
    single { ContestantsCache() }
    single { NetworkConnectivityHelper() }
    single { TopSnackBarHelper() }
    single(named("RenderState")) { ConflatedBroadcastChannel<IRenderState>() }
    single(named("ErrorMsg")) { BroadcastChannel<String>(Channel.BUFFERED) }
    single(named("NetworkState")) { BroadcastChannel<Boolean>(Channel.BUFFERED) }
    single(named("Notification")) { BroadcastChannel<ITopBarNotification>(Channel.BUFFERED) }
    single(named("NotificationDismiss")) { BroadcastChannel<TopSnackBarType>(Channel.BUFFERED) }
    factory { androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    factory<IContestantsDataSource> { ContestantsDataSource() }
    factory { GetRenderUiChannelInteractor() }
    factory { GetErrorMsgChannelInteractor() }
    factory { ChosenContestantInteractor() }
    factory { GetSuperCategoriesInteractor() }
    factory { GetCategoriesInteractor() }
    factory { GetQuizListInteractor() }
    factory { GetQuizItemDetailInteractor() }
    factory { GetCurrentQuizListInteractor() }
    factory { GetCurrentSuperCategoryInteractor() }
    factory { GetCurrentCategoryInteractor() }
    factory { GetCurrentQuizInteractor() }
    factory { ResetInteractor() }
    factory { GetStatsInteractor() }
    factory { CancelQuizInteractor() }
    factory { PageIndicatorTextInteractor() }
    factory { RetrieveChosenContestantInteractor() }
    factory { CurrentSuperCategoryInteractor() }
    factory { GetStatsOptionInteractor() }
    factory<IMainContract.IMainPresenter> { MainPresenter() }
    factory<IQuizListContract.IQuizListPresenter> { QuizListPresenter() }
    factory<IQuizPagerContract.IQuizPagerPresenter> { QuizPagerPresenter() }
    factory<IQuizPageContract.IQuizPagePresenter> { QuizPagePresenter() }
    factory<IQuizItemDetailContract.IQuizItemDetailPresenter> { QuizItemDetailPresenter() }
    factory<IWinnerContract.IWinnerPresenter> { WinnerPresenter() }
    factory<IErrorDialogContract.IErrorDialogPresenter> { ErrorDialogPresenter() }
    factory<IAboutDialogContract.IAboutDialogPresenter> { AboutDialogPresenter() }
    factory<ILicensesDialogContract.ILicensesDialogPresenter> { LicensesDialogPresenter() }
    factory<IEmptyPagerContract.IEmptyPagerPresenter> { EmptyPagerPresenter() }
}