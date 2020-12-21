package com.avp.ctbo.presentation.base

import com.avp.core.domain.IRenderState

interface IBaseView {
    suspend fun render(renderState: IRenderState) {}
}