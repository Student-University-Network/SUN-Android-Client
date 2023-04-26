package com.sun.sunclient.network.repository

import android.util.Log
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.MarkAttendanceInput
import com.sun.sunclient.network.schemas.MarkAttendanceResponse
import com.sun.sunclient.network.schemas.SetLectureStatusResponse
import com.sun.sunclient.network.schemas.TakeAttendanceInput
import com.sun.sunclient.network.service.AttendanceApiService
import com.sun.sunclient.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val api: AttendanceApiService,
    private val dataStore: AppDataStore
) {
    val TAG = "AuthRepository"

    val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch { readStoredData() }
    }

    suspend fun reset() {

    }

    private suspend fun readStoredData() {

    }

    suspend fun refreshCache() {
        readStoredData()
    }

    suspend fun takeAttendance(lectureId: String, courseId: String): String {
        return try {
            val response = api.takeAttendance(TakeAttendanceInput(lectureId, courseId))
            response.data.token
        } catch (e: Exception) {
            Log.e(TAG, "TakeAttendance: $e")
            ""
        }
    }

    suspend fun markAttendance(token: String): MarkAttendanceResponse {
        return try {
            val response = api.markAttendance(MarkAttendanceInput(token))
            response
        } catch (e: Exception) {
            Log.e(TAG, "markAttendance: $e")
            MarkAttendanceResponse("failed", null)
        }
    }
}