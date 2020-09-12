package com.rkhrapunov.versustest.presentation.topsnackbar

interface ITopBarNotification {
    val type: TopSnackBarType
    val notificationTextResId: Int
    val iconId: Int
    val unique: Boolean
    val notificationTimeout: Int
    val action: () -> Unit
}

data class TopBarNotification(
    override val type: TopSnackBarType,
    override val notificationTextResId: Int,
    override val iconId: Int,
    override val unique: Boolean = false,
    override val notificationTimeout: Int,
    override val action: () -> Unit
) : ITopBarNotification