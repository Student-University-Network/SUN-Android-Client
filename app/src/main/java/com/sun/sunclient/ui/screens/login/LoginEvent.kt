package com.sun.sunclient.ui.screens.login

sealed class LoginEvent {
    object Login: LoginEvent()
    object TogglePasswordVisibility: LoginEvent()
    data class OnPasswordChange(val password: String): LoginEvent()
    data class OnUsernameChange(val username: String): LoginEvent()
}
