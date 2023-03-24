package com.sun.sunclient.network.schemas

import java.util.Date

data class ProfileResponse(
    val status: String,
    val message: String = "",
    val data: UserData = UserData()
)

data class UserData(
    val userId: String = "",
    val firstName: String = "Unknown",
    val middleName: String? = null,
    val lastName: String = "",
    val gender: String? = "",
    val dateOfBirth: Date? = null,
)

data class ProfileUpdateInput(
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val gender: String?,
    val dateOfBirth: Date?,
)

data class ChangePasswordInput(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
);

data class ChangePasswordResponse(
    val status: String,
    val message: String,
)