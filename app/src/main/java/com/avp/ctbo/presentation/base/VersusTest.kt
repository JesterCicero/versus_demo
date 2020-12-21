package com.avp.ctbo.presentation.base

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.avp.ctbo.framework.di.applicationModule
import com.avp.ctbo.BuildConfig
import com.avp.ctbo.framework.helpers.NetworkConnectivityHelper
import com.avp.ctbo.presentation.topsnackbar.TopSnackBarHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@FlowPreview
class VersusTest : Application() {

    private val mConnectivityHelper by inject<NetworkConnectivityHelper>()
    private val mTopSnackBarHelper by inject<TopSnackBarHelper>()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        startKoin {
            androidContext(this@VersusTest)
            modules(applicationModule)
        }
        mConnectivityHelper.subscribeNetworkStatus()
        mTopSnackBarHelper.init()
    }
}