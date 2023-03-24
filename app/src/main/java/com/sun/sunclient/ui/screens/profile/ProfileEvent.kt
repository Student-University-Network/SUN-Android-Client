package com.sun.sunclient.ui.screens.profile

sealed class ProfileEvent {
    object EditPersonalDetails : ProfileEvent()
    object UpdatePersonalDetails: ProfileEvent()
    object ClosePersonalDetailsUpdate: ProfileEvent()
    object ShowChangePassword: ProfileEvent()
    object CloseChangePassword: ProfileEvent()
    object ChangePassword: ProfileEvent()

    // InputField events
    data class OnPersonalDetailsChange(val updatedState: ProfileState): ProfileEvent()
}