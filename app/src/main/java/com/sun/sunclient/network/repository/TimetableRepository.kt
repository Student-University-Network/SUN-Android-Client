package com.sun.sunclient.network.repository

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.TimetableApiService
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.parseJson
import com.sun.sunclient.utils.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class TimetableRepository @Inject constructor(
    private val api: TimetableApiService,
    private val dataStore: AppDataStore
) {
    private val TAG = "TimetableRepository"
    private val scope = CoroutineScope(Dispatchers.IO)

    var userData = LoginResponse.UserDetails()
        private set
    var timetableData = Timetable()
        private set

    init {
        scope.launch {
            readStoredData()
            refreshCache()
        }
    }

    suspend fun reset() {
        timetableData = Timetable()
        userData = LoginResponse.UserDetails()
        dataStore.saveString(Constants.TIMETABLE_KEY, "{}")
        dataStore.saveString(Constants.USER_DETAILS_KEY, "{}")
        dataStore.saveString(Constants.IS_TIMETABLE_SCHEDULED, "")
    }

    private suspend fun readStoredData() {
        var dataString = dataStore.readString(Constants.TIMETABLE_KEY).first()
        if (dataString != "") {
            timetableData = parseJson(
                dataString,
                TypeToken.get(Timetable::class.java)
            ) ?: Timetable()
        }
        dataString = dataStore.readString(Constants.USER_DETAILS_KEY).first()
        if (dataString != "") {
            userData = parseJson(dataString, TypeToken.get(LoginResponse.UserDetails::class.java))
                ?: LoginResponse.UserDetails()
        }
    }

    suspend fun refreshCache() {
        readStoredData()
        getTimetable()
    }

    private suspend fun getTimetable(): GetTimetableResponse {
        val batchId = parseJson(
            dataStore.readString(Constants.PROGRAM_DATA_KEY).first(),
            TypeToken.get(Program::class.java)
        )?.batchId ?: ""
        return try {
            val response: GetTimetableResponse = if (userData.role == Constants.FACULTY) {
                api.getFacultyTimetable()
            } else {
                api.getStudentTimetable(batchId)
            }
            timetableData = response.data ?: Timetable()
            dataStore.saveString(Constants.TIMETABLE_KEY, stringify(timetableData))
            response
        } catch (e: Exception) {
            Log.e(TAG, "GetTimetable: $e")
            GetTimetableResponse("failed", null);
        }
    }

    suspend fun setLectureStatus(payload: SetLectureInput) : SetLectureStatusResponse {
        return try {
            if (userData.role != Constants.FACULTY) throw Error()
            val response: SetLectureStatusResponse = api.setLectureStatus(payload)
            if (response.status == "success") {
                getTimetable()
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "SetLectureStatus: $e")
            SetLectureStatusResponse("failed");
        }
    }
}