package com.sun.sunclient.network.schemas

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    val statusCode : Int = 200,
    val status: String,
    val message: String,
    val username: String,
    val accessToken: String
)

data class RefreshResponse(
    val accessToken: String
)

data class LogoutResponse(
    val status: String,
    val message: String
)