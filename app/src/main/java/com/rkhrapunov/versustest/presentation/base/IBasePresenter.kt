package com.rkhrapunov.versustest.presentation.base

import androidx.lifecycle.Lifecycle

interface IBasePresenter<V : IBaseView> {
    fun attachView(view: V, viewLifecycle: Lifecycle)
    fun onDestroy() {}
}