package com.rkhrapunov.versustest.presentation.base

import android.app.Application
import com.rkhrapunov.versustest.framework.di.applicationModule
import com.rkhrapunov.versustest.BuildConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

@ExperimentalCoroutinesApi
@FlowPreview
class VersusTest : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        startKoin {
            androidContext(this@VersusTest)
            modules(applicationModule)
        }
    }
}