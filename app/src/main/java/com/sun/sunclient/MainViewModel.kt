package com.sun.sunclient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.repository.AuthRepository
import com.sun.sunclient.network.repository.UserRepository
import com.sun.sunclient.network.schemas.UserData
import com.sun.sunclient.utils.AppEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dataStore: AppDataStore,
) : ViewModel() {

    val TAG = "MainViewModel"

    var userData by mutableStateOf(userRepository.userData)
        private set

    fun onStart() {
        viewModelScope.launch {
            // TODO: do all data fetching on application start here
            if (dataStore.readAccessToken().first() != "") {
                MyEvents.eventFlow.send(AppEvent.OnLogin)
            }
            if (authRepository.refresh()) {
                if (userData.userId == "") {
                    userRepository.getUserProfile()
                }
                syncData()
            } else {
                logout()
            }
        }
    }

    fun setLoggedIn() {
        viewModelScope.launch { MyEvents.eventFlow.send(AppEvent.OnLogin) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            MyEvents.eventFlow.send(AppEvent.OnLogout)
        }
    }

    fun syncData() {
        viewModelScope.launch { userData = userRepository.userData }
    }
}