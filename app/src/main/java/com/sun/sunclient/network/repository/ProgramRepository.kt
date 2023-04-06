package com.sun.sunclient.network.repository

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.ProgramApiService
import com.sun.sunclient.utils.Constants.FACULTY_COURSES_KEY
import com.sun.sunclient.utils.Constants.PROGRAM_DATA_KEY
import com.sun.sunclient.utils.Constants.USER_DETAILS_KEY
import com.sun.sunclient.utils.parseJson
import com.sun.sunclient.utils.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgramRepository @Inject constructor(
    private val api: ProgramApiService,
    private val dataStore: AppDataStore
) {

    private val TAG = "ProgramRepository"
    private val scope = CoroutineScope(Dispatchers.IO)

    var programData = Program()
        private set
    var facultyCourseData: List<FacultyCourse> = ArrayList()
        private set

    init {
        scope.launch {
            readStoredData()
            refreshCache()
        }
    }

    suspend fun reset() {
        programData = Program()
        dataStore.saveString(PROGRAM_DATA_KEY, "{}")
        dataStore.saveString(FACULTY_COURSES_KEY, "[]")
    }

    private suspend fun readStoredData() {
        var dataString = dataStore.readString(PROGRAM_DATA_KEY).first()
        if (dataString != "") {
            programData = parseJson(dataString, TypeToken.get(Program::class.java)) ?: Program()
        }

        dataString = dataStore.readString(FACULTY_COURSES_KEY).first()
        if (dataString != "") {
            val tmp = parseJson(
                dataString,
                TypeToken.getParameterized(ArrayList::class.java, FacultyCourse::class.java)
            )
            facultyCourseData = if (tmp == null) {
                ArrayList()
            } else {
                tmp as List<FacultyCourse>
            }
        }
    }

    suspend fun refreshCache() {
        val programId = parseJson(
            dataStore.readString(USER_DETAILS_KEY).first(),
            TypeToken.get(Program::class.java)
        )?.programId ?: ""
        programData = programData.copy(programId = programId)
        getProgramDetails(programData.programId)
        getFacultyCourses()
    }

    suspend fun getProgramDetails(programId: String): GetProgramDetailsResponse {
        return try {
            val response = api.getProgramDetails(programId)
            programData = response.data
            dataStore.saveString(PROGRAM_DATA_KEY, stringify(programData))
            response
        } catch (e: Exception) {
            Log.e(TAG, "GetProgramDetails: $e")
            GetProgramDetailsResponse("failed", Program());
        }
    }

    suspend fun getFacultyCourses(): GetFacultyCoursesResponse {
        return try {
            val response = api.getFacultyCourses()
            facultyCourseData = response.data.courses
            dataStore.saveString(FACULTY_COURSES_KEY, stringify(facultyCourseData))
            response
        } catch (e: Exception) {
            Log.e(TAG, "GetFacultyCourses: $e")
            GetFacultyCoursesResponse("failed");
        }
    }
}