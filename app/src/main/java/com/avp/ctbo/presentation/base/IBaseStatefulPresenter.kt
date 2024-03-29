package com.avp.ctbo.presentation.base

import android.os.Bundle

interface IBaseStatefulPresenter<V: IBaseView> : IBasePresenter<V> {
    fun onSaveInstanceState(outState: Bundle?) {}
    fun onRestoreInstanceState(outState: Bundle?) {}
}