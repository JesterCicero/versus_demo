package com.rkhrapunov.versustest.presentation.base

import com.rkhrapunov.core.domain.IRenderState


interface IBaseView {
    fun render(renderState: IRenderState) {}
}