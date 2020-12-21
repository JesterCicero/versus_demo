package com.avp.ctbo.presentation.base

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber

abstract class BasePresenter<V : IBaseView> : IBaseStatefulPresenter<V>, LifecycleObserver {

    private var mViewLifecycle: Lifecycle? = null
    protected var mView: V? by weak()

    override fun attachView(view: V, viewLifecycle: Lifecycle, savedInstanceState: Bundle?) {
        mView = view
        viewLifecycle.addObserver(this)
        mViewLifecycle = viewLifecycle
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected open fun onViewDestroyed() {
        mView = null
        mViewLifecycle = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun onPaused() {
        Timber.d("onPaused")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResumed() {
        Timber.d("onResumed")
    }
}