package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.ChangePasswordInput
import com.sun.sunclient.network.schemas.ChangePasswordResponse
import com.sun.sunclient.network.schemas.ProfileResponse
import com.sun.sunclient.network.schemas.ProfileUpdateInput
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {

    @GET("user/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("user/profile")
    suspend fun updateProfile(@Body updatedProfile: ProfileUpdateInput) : ProfileResponse

    @PUT("user/password")
    suspend fun changeUserPassword(@Body newPassword: ChangePasswordInput) : ChangePasswordResponse

}