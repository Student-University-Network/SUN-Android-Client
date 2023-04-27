package com.sun.sunclient.network.repository

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.AttendanceApiService
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.parseJson
import com.sun.sunclient.utils.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val api: AttendanceApiService,
    private val dataStore: AppDataStore
) {
    val TAG = "AuthRepository"

    val scope = CoroutineScope(Dispatchers.IO)
    var studentReport: StudentReport? = null
    private set
    var facultyReport: FacultyReport? = null
        private set

    init {
        scope.launch { readStoredData() }
    }

    suspend fun reset() {
        studentReport = null
        facultyReport = null
        dataStore.saveString(Constants.ATTENDANCE_FACULTY_KEY, "{}")
        dataStore.saveString(Constants.ATTENDANCE_STUDENT_KEY, "{}")
    }

    private suspend fun readStoredData() {
        val studentDataString = dataStore.readString(Constants.ATTENDANCE_STUDENT_KEY).first()
        val facultyDataString = dataStore.readString(Constants.ATTENDANCE_FACULTY_KEY).first()
        if (studentDataString != "") {
            studentReport = parseJson(studentDataString, TypeToken.get(StudentReport::class.java))
        }
        if (facultyDataString != "") {
            facultyReport = parseJson(facultyDataString, TypeToken.get(FacultyReport::class.java))
        }
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

    suspend fun getAttendanceReport(role: String, batchId: String, courseId: String) {
        try {
            if (role == Constants.FACULTY) {
                val response = api.getFacultyReport(courseId, batchId)
                facultyReport = response.data
                dataStore.saveString(Constants.ATTENDANCE_FACULTY_KEY, stringify(response.data))
            } else {
                val response = api.getStudentReport()
                studentReport = response.data
                dataStore.saveString(Constants.ATTENDANCE_STUDENT_KEY, stringify(response.data))
            }
        } catch (e: Exception) {
            Log.e(TAG, "markAttendance: $e")
        }
    }
}