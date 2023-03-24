package com.sun.sunclient.ui.screens.profile

import java.util.*

// Keep nullable values null instead of empty string so that retrofit skips null values in
// POST body instead of sending empty strings
data class ProfileState(
    val firstName: String = "Unknown",
    val middleName: String? = null,
    val lastName: String = "",
    val gender: String? = null,
    val dateOfBirth: Date? = null,
    val isEditingPersonalDetails: Boolean = false,
    val isChangePasswordVisible: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
)
