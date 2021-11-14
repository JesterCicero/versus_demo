package com.avp.ctbo.framework.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.avp.core.data.ContestantsRepository
import com.avp.core.data.IContestantsDataSource
import com.avp.core.domain.IRenderState
import com.avp.core.domain.RenderState
import com.avp.core.interactors.CancelQuizInteractor
import com.avp.core.interactors.ChosenContestantInteractor
import com.avp.core.interactors.CurrentSuperCategoryInteractor
import com.avp.core.interactors.GetCategoriesInteractor
import com.avp.core.interactors.GetCurrentCategoryInteractor
import com.avp.core.interactors.GetCurrentQuizInteractor
import com.avp.core.interactors.GetCurrentQuizListInteractor
import com.avp.core.interactors.GetCurrentRoundInteractor
import com.avp.core.interactors.GetCurrentSuperCategoryInteractor
import com.avp.core.interactors.GetErrorMsgChannelInteractor
import com.avp.core.interactors.GetQuizItemDetailInteractor
import com.avp.core.interactors.GetQuizListInteractor
import com.avp.core.interactors.GetRenderUiChannelInteractor
import com.avp.core.interactors.GetStatsInteractor
import com.avp.core.interactors.GetStatsOptionInteractor
import com.avp.core.interactors.GetSuperCategoriesInteractor
import com.avp.core.interactors.PageIndicatorTextInteractor
import com.avp.core.interactors.ResetInteractor
import com.avp.core.interactors.RetrieveChosenContestantInteractor
import com.avp.ctbo.framework.ContestantsCache
import com.avp.ctbo.framework.ContestantsDataSource
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.framework.helpers.NetworkConnectivityHelper
import com.avp.ctbo.framework.helpers.RestApiHelper
import com.avp.ctbo.presentation.about.AboutDialogPresenter
import com.avp.ctbo.presentation.about.IAboutDialogContract
import com.avp.ctbo.presentation.base.ImageLoader
import com.avp.ctbo.presentation.base.Preferences
import com.avp.ctbo.presentation.empty_pager.EmptyPagerPresenter
import com.avp.ctbo.presentation.empty_pager.IEmptyPagerContract
import com.avp.ctbo.presentation.error.ErrorDialogPresenter
import com.avp.ctbo.presentation.error.IErrorDialogContract
import com.avp.ctbo.presentation.licenses.ILicensesDialogContract
import com.avp.ctbo.presentation.licenses.LicensesDialogPresenter
import com.avp.ctbo.presentation.main.IMainContract
import com.avp.ctbo.presentation.main.MainPresenter
import com.avp.ctbo.presentation.quiz_detail.IQuizItemDetailContract
import com.avp.ctbo.presentation.quiz_detail.QuizItemDetailPresenter
import com.avp.ctbo.presentation.quiz_page.IQuizPageContract
import com.avp.ctbo.presentation.quiz_page.QuizPagePresenter
import com.avp.ctbo.presentation.quiz_pager.IQuizPagerContract
import com.avp.ctbo.presentation.quiz_pager.QuizPagerPresenter
import com.avp.ctbo.presentation.quizlist.IQuizListContract
import com.avp.ctbo.presentation.quizlist.QuizListPresenter
import com.avp.ctbo.presentation.topsnackbar.ITopBarNotification
import com.avp.ctbo.presentation.topsnackbar.TopSnackBarHelper
import com.avp.ctbo.presentation.topsnackbar.TopSnackBarType
import com.avp.ctbo.presentation.winner.IWinnerContract
import com.avp.ctbo.presentation.winner.WinnerPresenter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val PREFS_NAME = "prefs_name"

@DelicateCoroutinesApi
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val applicationModule = module {
    single { androidApplication() }
    single { ContestantsRepository() }
    single { CoroutineLauncherHelper() }
    single { RestApiHelper() }
    single { ImageLoader() }
    single { Preferences() }
    single { ContestantsCache() }
    single { NetworkConnectivityHelper() }
    single { TopSnackBarHelper() }
    single(named("RenderState")) { MutableStateFlow<IRenderState>(RenderState.SuperCategoriesState(
        emptyList())) }
    single(named("ErrorMsg")) { MutableSharedFlow<String>() }
    single(named("NetworkState")) { MutableSharedFlow<Boolean>() }
    single(named("Notification")) { MutableSharedFlow<ITopBarNotification>() }
    single(named("NotificationDismiss")) { MutableSharedFlow<TopSnackBarType>() }
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
    factory { GetCurrentRoundInteractor() }
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