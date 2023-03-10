package com.sun.sunclient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.network.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var showSplashScreen by mutableStateOf(false)
        private set

    var loggedIn by mutableStateOf(false)
        private set

    fun onStart() {
        viewModelScope.launch {
            // TODO: do all data fetching on application start here
            loggedIn = repository.refresh()
            showSplashScreen = true
        }
    }

    // TODO (temporary): replace with onEvent for MainViewModel
    fun setLoggedIn() {
        loggedIn = true
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            loggedIn = false
        }
    }
}