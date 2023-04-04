package com.sun.sunclient.network.schemas

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    val statusCode: Int = 200,
    val status: String,
    val message: String,
    val data: Data = Data()
) {
    data class Data(
        val accessToken: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val username: String = "",
        val id: String = "",
        val role: String = "",
        val programId: String = "",
    )
}

data class RefreshResponse(
    val accessToken: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val id: String,
    val role: String,
    val programId: String
)

data class LogoutResponse(
    val status: String,
    val message: String
)