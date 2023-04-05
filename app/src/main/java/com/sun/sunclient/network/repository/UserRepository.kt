package com.sun.sunclient.network.repository

import android.util.Log
import androidx.datastore.dataStore
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.UserApiService
import com.sun.sunclient.utils.parseJson
import com.sun.sunclient.utils.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApiService,
    private val dataStore: AppDataStore
) {
    val TAG = "UserRepository"
    val scope = CoroutineScope(Dispatchers.IO)

    var userProfile = UserData()
        private set

    init {
        scope.launch { val resp = refreshCache() }
    }

    suspend fun refreshCache() {
        val dataString = dataStore.readString("user-profile").first()
        if (dataString != "") {
            userProfile = parseJson(dataString, TypeToken.get(UserData::class.java))
        }
        val resp = getUserProfile()
    }

    fun reset() {
        userProfile = UserData()
    }

    suspend fun getUserProfile(): ProfileResponse {
        return try {
            val response = api.getProfile()
            userProfile = response.data
            dataStore.saveString("user-profile", stringify(userProfile))
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
            userProfile = response.data
            dataStore.saveString("user-profile", stringify(userProfile))
            response
        } catch (e: HttpException) {
            Log.e(TAG, "UpdateUserProfile ${e.response().toString()}")
            e.response()?.errorBody()?.string()?.let {
                // As error response is a array, wrap it with object to create valid JSON string
                val errorResponse = JSONObject("{\"data\":$it}")
                val jsonData = errorResponse.get("data")
                if (jsonData is JSONArray) {
                    message = errorResponse.getJSONArray("data").getJSONObject(0).getString("message")
                } else if (jsonData is JSONObject) {
                    message = errorResponse.getJSONObject("data").getString("message")
                }
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
                val jsonData = errorResponse.get("data")
                if (jsonData is JSONArray) {
                    message = errorResponse.getJSONArray("data").getJSONObject(0).getString("message")
                } else if (jsonData is JSONObject) {
                    message = errorResponse.getJSONObject("data").getString("message")
                }
            }
            ChangePasswordResponse("failed", message)
        } catch (e: Exception) {
            Log.e(TAG, "ChangeUserPassword: $e")
            ChangePasswordResponse("failed", message)
        }
    }
}