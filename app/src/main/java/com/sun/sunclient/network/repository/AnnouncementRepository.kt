package com.sun.sunclient.network.repository

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.network.service.AnnouncementsApiService
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.parseJson
import com.sun.sunclient.utils.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class AnnouncementRepository @Inject constructor(
    private val api: AnnouncementsApiService,
    private val dataStore: AppDataStore
) {
    val TAG = "AnnouncementRepository"

    val scope = CoroutineScope(Dispatchers.IO)
    var announcementsData: List<Announcement> = ArrayList()
        private set

    init {
        scope.launch {
            readStoredData()
            refreshCache()
        }
    }

    suspend fun reset() {
        announcementsData = ArrayList()
        dataStore.saveString(Constants.ANNOUNCEMENTS_KEY, "[]")
    }

    suspend fun refreshCache() {
        getAnnouncements()
    }

    private suspend fun readStoredData() {
        val dataString = dataStore.readString(Constants.ANNOUNCEMENTS_KEY).first()
        if (dataString != "") {
            val tmp = parseJson(
                dataString,
                TypeToken.getParameterized(ArrayList::class.java, Announcement::class.java)
            )
            announcementsData = if (tmp == null) {
                ArrayList()
            } else {
                tmp as List<Announcement>
            }
        }
    }

    suspend fun getAnnouncements() {
        try {
            val response = api.getAnnouncements()
            announcementsData = response.data.announcements
            dataStore.saveString(Constants.ANNOUNCEMENTS_KEY, stringify(announcementsData))
        } catch (e: Exception) {
            Log.e(TAG, "GetAnnouncements: $e")
        }
    }

    suspend fun setAnnouncements(payload: PostAnnouncementInput) {
        try {
            val response = api.postAnnouncement(payload)
        } catch (e: Exception) {
            Log.e(TAG, "SetAnnouncements: $e")
        }
    }

    suspend fun getAnnouncementsProgramList(): List<AnnouncementProgram> {
        return try {
            val response = api.getAnnouncementProgramsList()
            return response.data.programs
        } catch (e: Exception) {
            Log.e(TAG, "GetAnnouncementsProgramList: $e")
            ArrayList()
        }
    }
}