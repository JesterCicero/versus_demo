package com.avp.ctbo.presentation.base

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.avp.ctbo.framework.di.applicationModule
import com.avp.ctbo.BuildConfig
import com.avp.ctbo.framework.helpers.NetworkConnectivityHelper
import com.avp.ctbo.presentation.topsnackbar.TopSnackBarHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

@Suppress("unused")
@DelicateCoroutinesApi
class VersusTest : Application() {

    private val mConnectivityHelper by inject<NetworkConnectivityHelper>()
    private val mTopSnackBarHelper by inject<TopSnackBarHelper>()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
//        else {
//            Timber.plant(CrashReportingTree())
//        }
        startKoin {
            androidContext(this@VersusTest)
            modules(applicationModule)
        }
        mConnectivityHelper.subscribeNetworkStatus()
        mTopSnackBarHelper.init()
    }
}

@Suppress("unused")
class CrashReportingTree : Timber.Tree() {
    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.DEBUG || priority == Log.ERROR) {
            Log.e("TEST", message)
        }
    }
}