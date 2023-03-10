package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.LoginRequest
import com.sun.sunclient.network.schemas.LoginResponse
import com.sun.sunclient.network.schemas.LogoutResponse
import com.sun.sunclient.network.schemas.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest) : LoginResponse

    @GET("auth/refreshToken")
    suspend fun refresh() : RefreshResponse

    @GET("auth/test")
    suspend fun test(): String
    
    @POST("auth/logout")
    suspend fun logout() : LogoutResponse
}
