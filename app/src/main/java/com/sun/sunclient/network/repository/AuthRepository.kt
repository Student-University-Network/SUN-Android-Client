package com.sun.sunclient.network.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.LoginRequest
import com.sun.sunclient.network.schemas.LoginResponse
import com.sun.sunclient.network.service.AuthApiService
import com.sun.sunclient.utils.Constants.USER_DETAILS_KEY
import com.sun.sunclient.utils.parseJson
import com.sun.sunclient.utils.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val dataStore: AppDataStore
) {
    val TAG = "AuthRepository"

    val scope = CoroutineScope(Dispatchers.IO)

    var userData = LoginResponse.UserDetails()
        private set

    init {
        scope.launch { val resp = refreshCache() }
    }

    suspend fun refreshCache() {
        val dataString = dataStore.readString(USER_DETAILS_KEY).first()
        if (dataString != "") {
            userData = parseJson(dataString, TypeToken.get(LoginResponse.UserDetails::class.java)) ?: LoginResponse.UserDetails()
        }
        val resp = refresh()
    }

    suspend fun login(username: String, password: String): LoginResponse {
        var response: LoginResponse
        try {
            response =
                api.login(LoginRequest(username = username.trim(), password = password.trim()))
            dataStore.saveAccessToken(response.data.accessToken)
            userData = LoginResponse.UserDetails(
                firstName = response.data.firstName,
                lastName = response.data.lastName,
                id = response.data.id,
                programId = response.data.programId,
                username = response.data.username,
                role = response.data.role
            )
            dataStore.saveString(USER_DETAILS_KEY, stringify(userData))
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
            userData = LoginResponse.UserDetails(
                firstName = response.firstName,
                lastName = response.lastName,
                id = response.id,
                programId = response.programId,
                username = response.username,
                role = response.role
            )
            dataStore.saveString(USER_DETAILS_KEY, stringify(userData))
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
            dataStore.saveString(USER_DETAILS_KEY, "{}")
        }
    }
}