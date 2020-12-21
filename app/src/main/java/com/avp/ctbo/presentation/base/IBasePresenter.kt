package com.avp.ctbo.presentation.base

import android.os.Bundle
import androidx.lifecycle.Lifecycle

interface IBasePresenter<V : IBaseView> {
    fun attachView(view: V, viewLifecycle: Lifecycle, savedInstanceState: Bundle? = null)
    fun onDestroy() {}
}