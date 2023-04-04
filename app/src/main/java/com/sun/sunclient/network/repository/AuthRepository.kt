package com.sun.sunclient.network.repository

import android.util.Log
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.LoginRequest
import com.sun.sunclient.network.schemas.LoginResponse
import com.sun.sunclient.network.service.AuthApiService
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val dataStore: AppDataStore
) {
    val TAG = "AuthRepository"

    suspend fun login(username: String, password: String): LoginResponse {
        var response: LoginResponse
        try {
            response =
                api.login(LoginRequest(username = username.trim(), password = password.trim()))
            dataStore.saveAccessToken(response.data.accessToken)
        } catch (e: HttpException) {
            val code = e.response()?.code() ?: 500
            val message = when (e.response()?.code()) {
                401 -> "Username or password is incorrect"
                400 -> "Username or password is incorrect"
                else -> "Something went wrong. Please try again"
            }
            response = LoginResponse(code, "failed", message)
        } catch (e: Exception) {
            response =
                LoginResponse(500, "failed", "Server error !! Please try again later")
        }
        return response
    }

    suspend fun refresh(): Boolean {
        return try {
            val response = api.refresh()
            dataStore.saveAccessToken(response.accessToken)
            true
        } catch (e: HttpException) {
            // If response is Unauthorized then only auto logout
            false
        } catch (e: Exception) {
            // If cant connect then stay as logged in
            true
        }
    }

    suspend fun logout() {
        try {
            val response = api.logout()
        } catch (e: Exception) {
            Log.e(TAG, "Error in logout")
        } finally {
            dataStore.saveAccessToken("")
            dataStore.saveCookieSet(HashSet())
        }
    }
}