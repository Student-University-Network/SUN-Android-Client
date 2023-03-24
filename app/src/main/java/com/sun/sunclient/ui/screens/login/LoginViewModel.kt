package com.sun.sunclient.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.MyEvents
import com.sun.sunclient.network.repository.AuthRepository
import com.sun.sunclient.utils.AppEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> {
                login()
            }
            is LoginEvent.TogglePasswordVisibility -> {
                state = state.copy(isPasswordVisible = !state.isPasswordVisible)
            }
            is LoginEvent.OnUsernameChange -> {
                state = state.copy(username = event.username)
            }
            is LoginEvent.OnPasswordChange -> {
                state = state.copy(password = event.password)
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val response = repository.login(username = state.username.trim(), password = state.password.trim())
            if (response.statusCode == 200) {
                MyEvents.eventFlow.send(AppEvent.OnLogin)
                MyEvents.eventFlow.send(AppEvent.SnackBar("Welcome !!"))
            } else {
                MyEvents.eventFlow.send(AppEvent.SnackBar(response.message))
            }
            state = state.copy(isLoading = false)
        }
    }
}