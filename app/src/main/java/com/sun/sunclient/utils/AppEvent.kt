package com.sun.sunclient.utils

import androidx.compose.material3.SnackbarDuration

sealed class AppEvent {
    data class SnackBar(
        val text: String,
        val actionLabel: String = "OK",
        val showDismissAction: Boolean = false,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val resolve: () -> Unit = {}
    ) : AppEvent()
    data class Navigate(val route: Screen) : AppEvent()
    object PopBackStack : AppEvent()
    object OnLogin : AppEvent()
    object OnLogout : AppEvent()
//    object OnSyncData: AppEvent()
}