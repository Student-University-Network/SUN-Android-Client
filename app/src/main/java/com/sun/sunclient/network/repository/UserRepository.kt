package com.sun.sunclient.network.repository

import android.util.Log
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.UserApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApiService
) {
    val TAG = "UserRepository"
    val scope = CoroutineScope(Dispatchers.IO)

    var userData = UserData()
        private set

    init {
        scope.launch { val resp = getUserProfile() }
    }

    suspend fun getUserProfile(): ProfileResponse {
        return try {
            val response = api.getProfile()
            userData = response.data
            response
        } catch (e: Exception) {
            Log.e(TAG, "GetUserProfile: $e")
            ProfileResponse("failed", "Failed to get profile details");
        }
    }

    suspend fun updateUserProfile(data: ProfileUpdateInput): ProfileResponse {
        var message = "Failed to change password"

        return try {
            val response = api.updateProfile(data)
            userData = response.data
            response
        } catch (e : HttpException) {
            Log.e(TAG, "UpdateUserProfile ${e.response().toString()}")
            e.response()?.errorBody()?.string()?.let {
                // As error response is a array, wrap it with object to create valid JSON string
                val errorResponse = JSONObject("{\"data\":$it}")
                message = errorResponse.getJSONArray("data").getJSONObject(0).get("message") as String
            }
            ProfileResponse("failed", message)
        } catch (e: Exception) {
            Log.e(TAG, "UpdateUserProfile: $e")
            ProfileResponse("failed", message)
        }
    }

    suspend fun changePassword(data: ChangePasswordInput): ChangePasswordResponse {
        var message = "Failed to change password"

        return try {
            val response = api.changeUserPassword(data)
            response
        } catch (e: HttpException) {
            Log.e(TAG, "ChangeUserPassword ${e.response().toString()}")
            e.response()?.errorBody()?.string()?.let {
                // As error response is a array, wrap it with object to create valid JSON string
                val errorResponse = JSONObject("{\"data\":$it}")
                message = errorResponse.getJSONArray("data").getJSONObject(0).get("message") as String
            }
            ChangePasswordResponse("failed", message)
        } catch (e: Exception) {
            Log.e(TAG, "ChangeUserPassword: $e")
            ChangePasswordResponse("failed", message)
        }
    }
}