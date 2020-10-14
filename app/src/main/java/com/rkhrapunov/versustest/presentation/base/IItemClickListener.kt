package com.rkhrapunov.versustest.presentation.base

interface IItemClickListener {
    fun onItemClickedIntent(itemData: String, position: Int = 0)
}