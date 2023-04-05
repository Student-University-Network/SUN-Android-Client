package com.sun.sunclient.network.repository

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.ProgramApiService
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

    val TAG = "ProgramRepository"
    val scope = CoroutineScope(Dispatchers.IO)

    var programData = Program()
        private set

    init {
        scope.launch {
            refreshCache()
        }
    }

    suspend fun refreshCache() {
        val dataString = dataStore.readString("program-data").first()
        if (dataString != "") {
            programData = parseJson(dataString, TypeToken.get(Program::class.java))
        }
        val programId = parseJson(
            dataStore.readString("user-details").first(),
            TypeToken.get(Program::class.java)
        ).programId
        programData = programData.copy(programId = programId)
        getProgramDetails(programData.programId)
    }

    suspend fun getProgramDetails(programId: String): GetProgramDetailsResponse {
        return try {
            val response = api.getProgramDetails(programId)
            programData = response.data
            dataStore.saveString("program-data", stringify(programData))
            response
        } catch (e: Exception) {
            Log.e(TAG, "GetProgramDetails: $e")
            GetProgramDetailsResponse("failed", Program());
        }
    }
}