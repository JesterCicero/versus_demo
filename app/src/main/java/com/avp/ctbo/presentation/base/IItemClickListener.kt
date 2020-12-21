package com.avp.ctbo.presentation.base

interface IItemClickListener {
    fun onItemClickedIntent(itemData: String, position: Int = 0)
}