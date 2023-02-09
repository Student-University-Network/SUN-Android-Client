package com.sun.sunclient.ui.screens.login

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoggedIn: Boolean = false
)