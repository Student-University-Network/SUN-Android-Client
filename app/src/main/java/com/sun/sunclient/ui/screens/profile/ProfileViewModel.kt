package com.sun.sunclient.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.MyEvents
import com.sun.sunclient.network.repository.UserRepository
import com.sun.sunclient.network.schemas.ChangePasswordInput
import com.sun.sunclient.network.schemas.ProfileUpdateInput
import com.sun.sunclient.network.schemas.UserData
import com.sun.sunclient.utils.AppEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {

    val TAG = "ProfileViewModel"

    val genderOptions = listOf("None", "MALE", "FEMALE", "OTHER")
    var profileState by mutableStateOf(syncData(userRepository.userProfile))
        private set

    init {
        viewModelScope.launch {
            userRepository.getUserProfile()
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.EditPersonalDetails -> {
                profileState = profileState.copy(isEditingPersonalDetails = true)
            }
            is ProfileEvent.UpdatePersonalDetails -> {
                updatePersonalDetails()
            }
            is ProfileEvent.ClosePersonalDetailsUpdate -> {
                profileState = syncData(userRepository.userProfile)
                profileState = profileState.copy(isEditingPersonalDetails = false)
            }
            is ProfileEvent.ShowChangePassword -> {
                profileState = profileState.copy(isChangePasswordVisible = true)
            }
            is ProfileEvent.CloseChangePassword -> {
                profileState = profileState.copy(
                    isChangePasswordVisible = false,
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )
            }
            is ProfileEvent.ChangePassword -> {
                changeUserPassword()
            }
            is ProfileEvent.OnPersonalDetailsChange -> {
                profileState = event.updatedState.copy()
            }
            else -> {}
        }
    }

    private fun updatePersonalDetails() {
        viewModelScope.launch {
            // Important: In retrofit json converter day starts from value 0 to 29/30
            // but the Date/Calender class uses day from 1 to 30/31
            // hence we have to increase day by 1 to handle these difference
            val newDateOfBirth =
                if (profileState.dateOfBirth == null) null else Calendar.getInstance()
            newDateOfBirth?.time = profileState.dateOfBirth
            newDateOfBirth?.set(
                newDateOfBirth.get(Calendar.YEAR),
                newDateOfBirth.get(Calendar.MONTH),
                newDateOfBirth.get(Calendar.DAY_OF_MONTH) + 1
            )
            val response = userRepository.updateUserProfile(
                ProfileUpdateInput(
                    firstName = profileState.firstName,
                    middleName = profileState.middleName,
                    lastName = profileState.lastName,
                    gender = profileState.gender,
                    dateOfBirth = newDateOfBirth?.time,
                )
            )

            if (response.status == "success") {
                MyEvents.eventFlow.send(AppEvent.SnackBar("Profile updated successfully"))
                onEvent(ProfileEvent.ClosePersonalDetailsUpdate)
            } else {
                MyEvents.eventFlow.send(AppEvent.SnackBar(response.message))
            }
        }
    }

    private fun changeUserPassword() {
        viewModelScope.launch {
            val response = userRepository.changePassword(
                ChangePasswordInput(
                    profileState.currentPassword,
                    profileState.newPassword,
                    profileState.confirmPassword
                )
            )
            if (response.status == "success") {
                MyEvents.eventFlow.send(AppEvent.SnackBar("Password changed successfully"))
                onEvent(ProfileEvent.CloseChangePassword)
            } else {
                MyEvents.eventFlow.send(AppEvent.SnackBar(response.message))
            }
        }
    }

    private fun syncData(data: UserData): ProfileState {
        return ProfileState(
            firstName = data.firstName,
            middleName = data.middleName,
            lastName = data.lastName,
            gender = data.gender,
            dateOfBirth = data.dateOfBirth,
        )
    }
}