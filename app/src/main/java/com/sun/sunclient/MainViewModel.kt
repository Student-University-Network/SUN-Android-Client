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
import com.sun.sunclient.network.schemas.Semester
import com.sun.sunclient.utils.AppEvent
import com.sun.sunclient.utils.Screen
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
    var facultyCourses by mutableStateOf(programRepository.facultyCourseData)
        private set
    var userProfile by mutableStateOf(userRepository.userProfile)
        private set

    fun onStart() {
        viewModelScope.launch {
            if (dataStore.readAccessToken().first() != "") {
                MyEvents.eventFlow.send(AppEvent.OnLogin)
            }
            setGlobalData()
            if (authRepository.refresh()) {
                syncData()
            } else {
                logout()
            }
        }
    }

    fun setLoggedIn() {
        viewModelScope.launch {
            setGlobalData()
            syncData()
            MyEvents.eventFlow.send(AppEvent.Navigate(Screen.HOME, true))
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.reset()
            authRepository.logout()
            programRepository.reset()
            setGlobalData()
            MyEvents.eventFlow.send(AppEvent.OnLogout)
        }
    }

    fun syncData() {
        viewModelScope.launch {
            userRepository.refreshCache()
            programRepository.refreshCache()
            setGlobalData()
            MyEvents.eventFlow.send(AppEvent.OnSyncedData)
        }
    }

    private fun setGlobalData() {
        userData = authRepository.userData
        programData = programRepository.programData
        facultyCourses = programRepository.facultyCourseData
        userProfile = userRepository.userProfile
    }

    fun getCurrentSemester(): Semester {
        return programData.semesters.getOrElse(programData.currentSemester) { Semester() }
    }
}