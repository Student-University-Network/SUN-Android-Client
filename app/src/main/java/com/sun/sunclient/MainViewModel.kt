package com.sun.sunclient

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.repository.AuthRepository
import com.sun.sunclient.network.repository.ProgramRepository
import com.sun.sunclient.network.repository.UserRepository
import com.sun.sunclient.utils.AppEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val programRepository: ProgramRepository,
    private val dataStore: AppDataStore,
) : ViewModel() {

    val TAG = "MainViewModel"

    var userData by mutableStateOf(authRepository.userData)
    private set
    var programData by mutableStateOf(programRepository.programData)
        private set
    var userProfile by mutableStateOf(userRepository.userProfile)
        private set

    fun onStart() {
        viewModelScope.launch {
            // TODO: do all data fetching on application start here
            if (dataStore.readAccessToken().first() != "") {
                MyEvents.eventFlow.send(AppEvent.OnLogin)
            }
            if (authRepository.refresh()) {
                if (userProfile.userId == "") {
                    userRepository.refreshCache()
                }
                syncData()
            } else {
                logout()
            }
        }
    }

    fun setLoggedIn() {
        viewModelScope.launch {
            userRepository.refreshCache()
            syncData()
            MyEvents.eventFlow.send(AppEvent.OnLogin)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.reset()
            authRepository.logout()
            MyEvents.eventFlow.send(AppEvent.OnLogout)
        }
    }

    fun syncData() {
        viewModelScope.launch {
            if (userRepository.userProfile.userId == "") {
                userRepository.refreshCache()
                programRepository.refreshCache()
            }
            userData = authRepository.userData
            programData = programRepository.programData
            userProfile = userRepository.userProfile
        }
    }
}